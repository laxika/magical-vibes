#!/usr/bin/env pwsh
# Cursor beforeShellExecution / pre-tool-use hook: deny any Task/tool launch
# that requests a "-fast" model. Reads the hook payload as JSON on stdin and
# writes a permission decision as JSON on stdout.

$ErrorActionPreference = 'Stop'

# Read the entire hook payload from stdin.
$raw = [Console]::In.ReadToEnd()

$model = $null
if ($raw) {
    try {
        $model = ($raw | ConvertFrom-Json).tool_input.model
    } catch {
        # Malformed / empty payload: fall through and allow.
        $model = $null
    }
}

if ($model -and $model -like '*-fast*') {
    $response = [ordered]@{
        permission    = 'deny'
        user_message  = '-fast models are disabled in this project.'
        agent_message = 'Do not use -fast models. Relaunch the Task with composer-2.5 or omit the model field.'
    }
    $response | ConvertTo-Json -Compress
    exit 0
}

@{ permission = 'allow' } | ConvertTo-Json -Compress
exit 0
