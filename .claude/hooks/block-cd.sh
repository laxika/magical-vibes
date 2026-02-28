#!/bin/bash
# Blocks Bash commands that start with "cd "

COMMAND=$(jq -r '.tool_input.command')

if echo "$COMMAND" | grep -q '^cd '; then
  jq -n '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: "Do not use cd. The working directory is already correct."
    }
  }'
fi
