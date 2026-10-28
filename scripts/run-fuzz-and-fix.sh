#!/usr/bin/env bash
# Examples:
#   ./scripts/run-fuzz-and-fix.sh
#   ./scripts/run-fuzz-and-fix.sh --rounds 500 --effort high
#   ./scripts/run-fuzz-and-fix.sh --agent codex --model gpt-5.6-luna --effort xhigh
#
# Options:
#   --rounds N     Number of random AI games to run (1-1000000, default 5000).
#   --agent NAME   Coding agent CLI used to diagnose and fix a failed fuzz run: claude or codex (default claude).
#   --model NAME   Model used by the repair instance. Defaults to claude-opus-5-5 for Claude and gpt-6.1-sol for Codex.
#   --effort LVL   Reasoning effort: low, medium, high, xhigh or max. Defaults to medium for Claude and high for Codex.
#
# Requires bash, git, python3 (for JSON event parsing) and the selected agent CLI on PATH.

set -uo pipefail

rounds=5000
agent="claude"
model=""
effort=""

usage() {
    sed -n '2,13p' "$0" | sed 's/^# \{0,1\}//'
}

die() {
    echo "ERROR: $*" >&2
    exit 1
}

warn() {
    echo "WARNING: $*" >&2
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --rounds | -Rounds) rounds="${2-}"; shift 2 ;;
        --agent | -Agent) agent="${2-}"; shift 2 ;;
        --model | -Model) model="${2-}"; shift 2 ;;
        --effort | -Effort) effort="${2-}"; shift 2 ;;
        -h | --help) usage; exit 0 ;;
        *) usage >&2; die "Unknown argument: $1" ;;
    esac
done

if ! [[ "$rounds" =~ ^[0-9]+$ ]] || (( rounds < 1 || rounds > 1000000 )); then
    die "--rounds must be an integer between 1 and 1000000."
fi
case "$agent" in
    claude | codex) ;;
    *) die "--agent must be 'claude' or 'codex'." ;;
esac
case "$effort" in
    "" | low | medium | high | xhigh | max) ;;
    *) die "--effort must be one of: low, medium, high, xhigh, max." ;;
esac

if [[ "$agent" == "claude" ]]; then
    agent_name="Claude"
    model="${model:-claude-opus-5-5}"
    effort="${effort:-medium}"
    if [[ "$model" == "claude-opus-5-5" ]]; then
        commit_co_author="Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
    else
        commit_co_author="Co-Authored-By: Claude <noreply@anthropic.com>"
    fi
else
    agent_name="Codex"
    model="${model:-gpt-6.1-sol}"
    effort="${effort:-high}"
    commit_co_author="Co-authored-by: OpenAI Codex <codex@openai.com>"
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
gradle_wrapper="$repository_root/gradlew"
log_path="$repository_root/fuzz.log"
test_class="com.github.laxika.magicalvibes.ai.RandomAiFuzzTest"
failure_pattern='^FAILURE:|^BUILD FAILED|^RandomAiFuzzTest > .* FAILED$|^> Task :magical-vibes-ai:test FAILED$'

[[ -f "$gradle_wrapper" ]] || die "Gradle wrapper not found at $gradle_wrapper."
command -v "$agent" > /dev/null 2>&1 || die "The '$agent' CLI was not found on PATH."
command -v git > /dev/null 2>&1 || die "The 'git' CLI was not found on PATH."
command -v python3 > /dev/null 2>&1 || die "The 'python3' interpreter was not found on PATH."

branch="$(git -C "$repository_root" branch --show-current)" || die "Could not determine the current Git branch."
[[ "$branch" == "main" ]] || die "Fuzz repairs must run on main; the current branch is '$branch'."

initial_status="$(git -C "$repository_root" status --porcelain --untracked-files=all)" || die "Could not inspect the Git working tree."
[[ -z "$initial_status" ]] || die "The working tree must be clean before an automated fuzz repair can run."

starting_head="$(git -C "$repository_root" rev-parse HEAD)" || die "Could not determine the starting Git commit."

work_dir="$(mktemp -d -t magical-vibes-fuzz.XXXXXXXX)" || die "Could not create a temporary directory."
agent_pid=""
cleanup() {
    if [[ -n "$agent_pid" ]] && kill -0 "$agent_pid" 2> /dev/null; then
        kill "$agent_pid" 2> /dev/null
        wait "$agent_pid" 2> /dev/null
    fi
    rm -rf "$work_dir"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

commit_output_path="$work_dir/commit.json"
commit_schema_path="$work_dir/schema.json"
commit_message_path="$work_dir/message.txt"
prompt_path="$work_dir/prompt.txt"
agent_stream_path="$work_dir/agent-stream"

cat > "$commit_schema_path" << 'EOF'
{
  "type": "object",
  "properties": {
    "subject": {
      "type": "string",
      "description": "An imperative, descriptive Git commit subject of at most 72 characters.",
      "maxLength": 72,
      "pattern": "^[^\\r\\n]+$"
    },
    "body": {
      "type": "string",
      "description": "One to three concise sentences explaining the root cause, fix, and regression coverage."
    }
  },
  "required": ["subject", "body"],
  "additionalProperties": false
}
EOF

# Reads the agent's JSON event stream from stdin, echoes progress, and stops at the
# terminal event. Exit codes: 0 = turn completed, 10 = turn failed, 11 = stream ended
# without a terminal event.
read -r -d '' event_parser << 'PY'
import io, json, sys

agent, output_path = sys.argv[1], sys.argv[2]
stream = io.TextIOWrapper(sys.stdin.buffer, encoding="utf-8", errors="replace")

def out(text):
    print(text, flush=True)

def warn(text):
    print("WARNING: " + text, file=sys.stderr, flush=True)

for raw in stream:
    line = raw.rstrip("\r\n")
    try:
        event = json.loads(line)
        if not isinstance(event, dict):
            raise ValueError
    except ValueError:
        out(line)
        continue

    kind = event.get("type")
    if agent == "claude":
        if kind == "system":
            if event.get("subtype") == "init":
                out(f"Claude repair session started: {event.get('session_id')}")
        elif kind == "assistant":
            for content in (event.get("message") or {}).get("content") or []:
                tool_input = content.get("input") or {}
                if content.get("type") == "text" and content.get("text"):
                    out(content["text"])
                elif content.get("type") == "tool_use":
                    if content.get("name") == "Bash" and tool_input.get("command"):
                        out(f"Claude command: {tool_input['command']}")
                    elif tool_input.get("file_path"):
                        out(f"Claude {content.get('name')}: {tool_input['file_path']}")
                    elif content.get("name") != "StructuredOutput":
                        out(f"Claude tool: {content.get('name')}")
        elif kind == "result":
            usage = event.get("usage") or {}
            summary = (f"input: {usage.get('input_tokens')}, cached: {usage.get('cache_read_input_tokens')}, "
                       f"output: {usage.get('output_tokens')}, cost: ${event.get('total_cost_usd')}")
            if event.get("subtype") == "success" and not event.get("is_error") and event.get("structured_output"):
                with open(output_path, "w", encoding="utf-8") as f:
                    json.dump(event["structured_output"], f, separators=(",", ":"))
                out(f"Claude repair session completed ({summary}).")
                sys.exit(0)
            warn(f"Claude repair session failed ({event.get('subtype')}; {summary}): {event.get('result')}")
            sys.exit(10)
    else:
        if kind == "thread.started":
            out(f"Codex repair thread started: {event.get('thread_id')}")
        elif kind == "item.completed":
            item = event.get("item") or {}
            if item.get("type") == "agent_message":
                out(item.get("text"))
            elif item.get("type") == "command_execution":
                out(f"Codex command {item.get('status')}: {item.get('command')}")
        elif kind == "error":
            warn(f"Codex reported an error: {event.get('message')}")
        elif kind == "turn.failed":
            warn("Codex repair turn failed.")
            sys.exit(10)
        elif kind == "turn.completed":
            usage = event.get("usage") or {}
            out(f"Codex repair turn completed (input: {usage.get('input_tokens')}, "
                f"cached: {usage.get('cached_input_tokens')}, output: {usage.get('output_tokens')}).")
            sys.exit(0)

sys.exit(11)
PY

# Waits up to $2 seconds for PID $1 to exit; returns 1 if it is still running.
wait_for_exit() {
    local pid="$1" deadline=$((SECONDS + $2))
    while kill -0 "$pid" 2> /dev/null; do
        (( SECONDS >= deadline )) && return 1
        sleep 0.2
    done
    return 0
}

while true; do
    echo "Running $rounds Random AI fuzz games. Full output: $log_path"

    (
        cd "$repository_root" &&
            sh ./gradlew :magical-vibes-ai:test --tests "$test_class" -DrunCardFuzz=true -DfuzzGames="$rounds" \
                -Dorg.gradle.jvmargs=-Xmx6g --info --rerun --console=plain > "$log_path" 2>&1
    )
    gradle_exit_code=$?

    [[ -f "$log_path" ]] || die "The fuzz command produced no log at $log_path."

    log_shows_success=false
    log_shows_failure=false
    grep -qF "All $rounds fuzz games passed." "$log_path" && log_shows_success=true
    grep -qE "$failure_pattern" "$log_path" && log_shows_failure=true

    if [[ $gradle_exit_code -eq 0 && $log_shows_success == true && $log_shows_failure == false ]]; then
        echo "PASS: all $rounds fuzz games completed successfully."
        exit 0
    fi

    warn "The fuzz run failed or did not reach its success marker (Gradle exit code $gradle_exit_code)."
    failed_game_index="$(grep -E '^=== Game #[0-9]+/[0-9]+ ===$' "$log_path" | tail -n 1 | sed -E 's/^=== Game #([0-9]+)\/.*/\1/')"
    if [[ -n "$failed_game_index" ]]; then
        warn "Game #$failed_game_index failed."
    else
        warn "The failed game index could not be determined from $log_path."
    fi
    echo "Relevant log lines:"
    grep -E "$failure_pattern|^All [0-9]+ fuzz games passed\.$" "$log_path" | tail -n 20 | sed 's/^/  /'
    echo "Starting a fresh $agent_name repair instance (model: $model, effort: $effort)..."

    rm -f "$commit_output_path" "$commit_message_path" "$agent_stream_path"

    cat > "$prompt_path" << EOF
The RandomAiFuzzTest run for $rounds games failed with Gradle exit code $gradle_exit_code.

Inspect the complete fuzz log at fuzz.log and any test results generated by this run under magical-vibes-ai/build/test-results/test (verify result timestamps before relying on them). Diagnose the root cause and implement a rules-correct fix. Follow AGENTS.md and preserve unrelated working-tree changes. Do not stage, commit, push, or switch branches; this script will commit and push after you finish. Add focused behavioral regression tests where practical. If the fix changes AiDecisionEngine.java, always add or update focused integration coverage in EasyAiDecisionEngineTest, MediumAiDecisionEngineTest, and HardAiDecisionEngineTest; do not rely only on AiDecisionEngineTest. Do not run the full test suite or rerun the 500-game fuzz test; run only focused tests relevant to the fix. If Magic rules are ambiguous, verify the official ruling before changing behavior. Do not ask clarifying questions or stop at a diagnosis: make the best fix you can and verify it.

In your final response, provide a concise imperative commit subject and a descriptive body based on the actual root cause, fix, and tests. Never spell out a card set's full name; use only its short set code. Do not include a Co-authored-by trailer because the script adds it.
EOF

    if [[ "$agent" == "claude" ]]; then
        # Claude takes the schema inline and reports the structured output on its
        # final result event, which the event parser writes to $commit_output_path.
        compact_commit_schema="$(python3 -c 'import json, sys; print(json.dumps(json.load(open(sys.argv[1])), separators=(",", ":")))' "$commit_schema_path")" ||
            die "Could not compact the commit schema."
        agent_arguments=(
            --print
            --model "$model"
            --effort "$effort"
            --dangerously-skip-permissions
            --output-format stream-json
            --verbose
            --json-schema "$compact_commit_schema"
        )
    else
        agent_arguments=(
            --search
            --ask-for-approval never
            exec
            --ephemeral
            --json
            --model "$model"
            --config "model_reasoning_effort=\"$effort\""
            --cd "$repository_root"
            --output-schema "$commit_schema_path"
            --output-last-message "$commit_output_path"
            -
        )
    fi

    # Drive automation from the agent's terminal JSON event instead of waiting for
    # every descendant to release the stdout pipe after a long tool run.
    mkfifo "$agent_stream_path" || die "Could not create the agent output pipe."
    (cd "$repository_root" && exec "$agent" "${agent_arguments[@]}") < "$prompt_path" > "$agent_stream_path" &
    agent_pid=$!

    python3 -c "$event_parser" "$agent" "$commit_output_path" < "$agent_stream_path"
    parser_exit_code=$?

    if [[ $parser_exit_code -eq 0 || $parser_exit_code -eq 10 ]]; then
        if ! wait_for_exit "$agent_pid" 10; then
            warn "$agent_name finished the repair turn but did not exit within 10 seconds; terminating the stalled CLI process."
            kill "$agent_pid" 2> /dev/null
        fi
        wait "$agent_pid" 2> /dev/null
        agent_process_exit_code=$?
        if [[ $parser_exit_code -eq 0 ]]; then
            agent_exit_code=0
        else
            agent_exit_code=$agent_process_exit_code
            [[ $agent_exit_code -eq 0 ]] && agent_exit_code=1
        fi
    else
        wait "$agent_pid" 2> /dev/null
        agent_exit_code=$?
        [[ $agent_exit_code -eq 0 ]] && agent_exit_code=1
    fi
    agent_pid=""
    rm -f "$agent_stream_path"

    if [[ $agent_exit_code -ne 0 ]]; then
        echo "ERROR: $agent_name exited with code $agent_exit_code while attempting the fuzz fix." >&2
        exit "$agent_exit_code"
    fi

    current_branch="$(git -C "$repository_root" branch --show-current)"
    if [[ $? -ne 0 || "$current_branch" != "main" ]]; then
        die "$agent_name did not leave the repository on main; refusing to commit."
    fi

    current_head="$(git -C "$repository_root" rev-parse HEAD)"
    if [[ $? -ne 0 || "$current_head" != "$starting_head" ]]; then
        die "$agent_name changed Git history; refusing to create another automated commit."
    fi

    repair_status="$(git -C "$repository_root" status --porcelain --untracked-files=all)" || die "Could not inspect the repair working tree."
    [[ -n "$repair_status" ]] || die "$agent_name completed without producing a repair diff; nothing will be committed."

    [[ -f "$commit_output_path" ]] || die "$agent_name produced no commit metadata."

    commit_subject="$(python3 -c 'import json, sys; print(str(json.load(open(sys.argv[1], encoding="utf-8"))["subject"]).strip())' "$commit_output_path" 2>&1)" ||
        die "$agent_name produced invalid commit metadata: $commit_subject"
    commit_body="$(python3 -c 'import json, sys; print(str(json.load(open(sys.argv[1], encoding="utf-8"))["body"]).strip())' "$commit_output_path" 2>&1)" ||
        die "$agent_name produced invalid commit metadata: $commit_body"
    if [[ -z "${commit_subject//[[:space:]]/}" || -z "${commit_body//[[:space:]]/}" ]]; then
        die "$agent_name produced an empty commit subject or body."
    fi

    git -C "$repository_root" add --all || die "Could not stage the fuzz repair."

    git -C "$repository_root" diff --cached --quiet
    diff_exit_code=$?
    [[ $diff_exit_code -ne 0 ]] || die "The fuzz repair produced no staged changes."
    [[ $diff_exit_code -eq 1 ]] || die "Could not inspect the staged fuzz repair."

    printf '%s\n\n%s\n\n%s\n' "$commit_subject" "$commit_body" "$commit_co_author" > "$commit_message_path"

    git -C "$repository_root" commit --file "$commit_message_path" || die "Could not commit the fuzz repair."

    committed_head="$(git -C "$repository_root" rev-parse --short HEAD)"
    echo "Committed fuzz repair as $committed_head. Pushing main to origin..."

    git -C "$repository_root" push origin main ||
        die "The fuzz repair was committed as $committed_head, but pushing origin/main failed."

    starting_head="$(git -C "$repository_root" rev-parse HEAD)" ||
        die "Could not determine the repaired Git commit before starting another fuzz run."

    echo "$agent_name repaired the fuzz failure, committed $committed_head, and pushed origin/main. Starting another fuzz run."
done
