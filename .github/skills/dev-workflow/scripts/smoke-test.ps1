# Starts the app on a spare port, calls every endpoint, checks status codes, then stops the app.
# Add a line to $checks for each new endpoint.
param([int]$Port = 8089, [int]$StartupTimeoutSec = 150)

$ErrorActionPreference = 'Stop'
$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')
$base = "http://localhost:$Port"

$checks = @(
    @{ Name = 'List users';            Method = 'GET';    Path = '/api/users';                   Expect = 200 },
    @{ Name = 'Count users';           Method = 'GET';    Path = '/api/users/count';             Expect = 200 },
    @{ Name = 'Get user 1';            Method = 'GET';    Path = '/api/users/1';                 Expect = 200 },
    @{ Name = 'Get missing user';      Method = 'GET';    Path = '/api/users/999';               Expect = 404 },
    @{ Name = 'Create user';           Method = 'POST';   Path = '/api/users';                   Expect = 201; Body = '{"name":"Smoke Test","email":"smoke@example.com","role":"USER"}' },
    @{ Name = 'Create duplicate email'; Method = 'POST';  Path = '/api/users';                   Expect = 409; Body = '{"name":"Fake Alice","email":"ALICE@example.com","role":"USER"}' },
    @{ Name = 'Update user 1';         Method = 'PUT';    Path = '/api/users/1';                 Expect = 200; Body = '{"name":"Alice Updated","email":"alice@example.com","role":"ADMIN"}' },
    @{ Name = 'Update to taken email'; Method = 'PUT';    Path = '/api/users/1';                 Expect = 409; Body = '{"name":"Alice","email":"bob@example.com","role":"ADMIN"}' },
    @{ Name = 'Update missing user';   Method = 'PUT';    Path = '/api/users/999';               Expect = 404; Body = '{"name":"X","email":"x@example.com","role":"USER"}' },
    @{ Name = 'Search users';          Method = 'GET';    Path = '/api/users/search?name=alice'; Expect = 200 },
    @{ Name = 'Search without name';   Method = 'GET';    Path = '/api/users/search';            Expect = 400 },
    @{ Name = 'Delete user 2';         Method = 'DELETE'; Path = '/api/users/2';                 Expect = 204 },
    @{ Name = 'Delete missing user';   Method = 'DELETE'; Path = '/api/users/999';               Expect = 404 }
)

$log = Join-Path $env:TEMP "springai-smoke-$Port.log"
$errLog = "$log.err"
$proc = Start-Process -FilePath (Join-Path $projectRoot 'mvnw.cmd') `
    -ArgumentList 'spring-boot:run', "-Dspring-boot.run.arguments=--server.port=$Port" `
    -WorkingDirectory $projectRoot -RedirectStandardOutput $log -RedirectStandardError $errLog `
    -PassThru -WindowStyle Hidden

$failed = 0
try {
    $deadline = (Get-Date).AddSeconds($StartupTimeoutSec)
    $up = $false
    while ((Get-Date) -lt $deadline -and -not $proc.HasExited) {
        $code = & curl.exe -s -o NUL -w '%{http_code}' "$base/api/users/count"
        if ($code -eq '200') { $up = $true; break }
        Start-Sleep -Seconds 2
    }
    if (-not $up) {
        Write-Host "App did not start on port $Port. Last log lines:" -ForegroundColor Red
        Get-Content $log -Tail 30 -ErrorAction SilentlyContinue
        exit 1
    }

    foreach ($c in $checks) {
        $curlArgs = @('-s', '-o', 'NUL', '-w', '%{http_code}', '-X', $c.Method)
        $bodyFile = $null
        if ($c.Body) {
            # Body goes through a file to avoid PowerShell quote-escaping issues.
            $bodyFile = [System.IO.Path]::GetTempFileName()
            [System.IO.File]::WriteAllText($bodyFile, $c.Body)
            $curlArgs += @('-H', 'Content-Type: application/json', '--data-binary', "@$bodyFile")
        }
        $actual = [int](& curl.exe @curlArgs "$base$($c.Path)")
        if ($bodyFile) { Remove-Item $bodyFile -ErrorAction SilentlyContinue }

        if ($actual -eq $c.Expect) {
            Write-Host ("PASS  {0,-22} {1,-6} {2} -> {3}" -f $c.Name, $c.Method, $c.Path, $actual) -ForegroundColor Green
        } else {
            $failed++
            Write-Host ("FAIL  {0,-22} {1,-6} {2} -> {3} (expected {4})" -f $c.Name, $c.Method, $c.Path, $actual, $c.Expect) -ForegroundColor Red
        }
    }
}
finally {
    & taskkill.exe /PID $proc.Id /T /F 2>&1 | Out-Null
}

Write-Host ("Smoke test: {0}/{1} passed" -f ($checks.Count - $failed), $checks.Count)
if ($failed -gt 0) { exit 1 }
