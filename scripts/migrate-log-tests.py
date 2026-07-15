#!/usr/bin/env python3
"""Migrate tests for GameLogEntry-only logAndBroadcast API."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEST_SRC = ROOT / "magical-vibes-application" / "src" / "test" / "java"
GAME_LOG_IMPORT = "import com.github.laxika.magicalvibes.model.GameLog;"
GAME_LOG_ENTRY_IMPORT = "import com.github.laxika.magicalvibes.model.GameLogEntry;"
METHOD_PREFIX = "logAndBroadcast("
GAME_DATA_FIRST_ARG = re.compile(r"^(?:gameData|gd|.+\\.gameData\\(\\))$")


def find_matching_paren(text: str, open_index: int) -> int:
    depth = 0
    in_string = False
    escape = False
    i = open_index
    while i < len(text):
        ch = text[i]
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            i += 1
            continue
        if ch == '"':
            in_string = True
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError(f"Unbalanced parentheses at {open_index}")


def split_first_comma(expr: str) -> tuple[str, str]:
    depth = 0
    in_string = False
    escape = False
    for i, ch in enumerate(expr):
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
        elif ch == "," and depth == 0:
            return expr[:i].strip(), expr[i + 1 :].strip()
    raise ValueError("No top-level comma found")


def is_game_log_entry_expr(expr: str) -> bool:
    stripped = expr.lstrip()
    return (
        stripped.startswith("GameLogEntry.")
        or stripped.startswith("GameLog.")
        or (stripped.endswith(".build()") and "Builder" in stripped)
        or (stripped.endswith(".build()") and "logBuilder" in stripped)
    )


def migrate_direct_calls(content: str) -> tuple[str, int]:
    changes = 0
    search_from = 0
    while True:
        idx = content.find(METHOD_PREFIX, search_from)
        if idx == -1:
            break
        call_open = idx + len(METHOD_PREFIX) - 1
        call_close = find_matching_paren(content, call_open)
        inner = content[call_open + 1 : call_close]
        first_arg, second_arg = split_first_comma(inner)
        if not GAME_DATA_FIRST_ARG.match(first_arg):
            search_from = call_close + 1
            continue
        if is_game_log_entry_expr(second_arg):
            search_from = call_close + 1
            continue
        wrapped = f"logAndBroadcast({first_arg}, GameLog.text({second_arg}))"
        content = content[:idx] + wrapped + content[call_close + 1 :]
        changes += 1
        search_from = idx + len(wrapped)
    return content, changes


def ensure_imports(content: str) -> str:
    if "GameLog." in content and GAME_LOG_IMPORT not in content:
        content = re.sub(
            r"(package [^;]+;\n)",
            r"\1" + GAME_LOG_IMPORT + "\n",
            content,
            count=1,
        )
    if "GameLogEntry" in content and GAME_LOG_ENTRY_IMPORT not in content:
        if GAME_LOG_IMPORT in content:
            content = content.replace(
                GAME_LOG_IMPORT + "\n",
                GAME_LOG_IMPORT + "\n" + GAME_LOG_ENTRY_IMPORT + "\n",
                1,
            )
        else:
            content = re.sub(
                r"(package [^;]+;\n)",
                r"\1" + GAME_LOG_ENTRY_IMPORT + "\n",
                content,
                count=1,
            )
    if "any(GameLogEntry.class)" in content and "import static org.mockito.ArgumentMatchers.any;" not in content:
        if "import static org.mockito.Mockito." in content:
            content = content.replace(
                "import static org.mockito.Mockito.",
                "import static org.mockito.ArgumentMatchers.any;\nimport static org.mockito.Mockito.",
                1,
            )
    return content


def migrate_test_content(content: str) -> tuple[str, int]:
    changes = 0
    original = content

    replacements = [
        (r"argThat\(\(String msg\) ->", r"argThat((GameLogEntry entry) ->"),
        (r"argThat\(\(String s\) ->", r"argThat((GameLogEntry entry) ->"),
        (r"argThat\(\(String message\) ->", r"argThat((GameLogEntry entry) ->"),
        (r"argThat\(\(String log\) ->", r"argThat((GameLogEntry entry) ->"),
        (r"argThat\(\(String logEntry\) ->", r"argThat((GameLogEntry entry) ->"),
        (r"argThat\(\(String logMsg\) ->", r"argThat((GameLogEntry entry) ->"),
        (r"any\(String\.class\)", r"any(GameLogEntry.class)"),
        (r"org\.mockito\.ArgumentMatchers\.anyString\(\)", r"any(GameLogEntry.class)"),
    ]
    for pattern, repl in replacements:
        content, n = re.subn(pattern, repl, content)
        changes += n

    # Only replace anyString on lines that mention logAndBroadcast
    lines = content.splitlines(keepends=True)
    rebuilt = []
    for line in lines:
        if "logAndBroadcast" in line and "anyString()" in line:
            line, n = re.subn(r"(?<![\w.])anyString\(\)", "any(GameLogEntry.class)", line)
            changes += n
        rebuilt.append(line)
    content = "".join(rebuilt)

    if "argThat((GameLogEntry entry) ->" in content:
        for var in ("msg", "message", "logEntry", "logMsg", "log"):
            content = re.sub(rf"\b{var}\.", "entry.plainText().", content)
            content = re.sub(
                rf"(argThat\(\(GameLogEntry entry\) ->[^)]*?)\b{var}\b",
                r"\1entry.plainText()",
                content,
            )
        content = re.sub(r"(argThat\(\(GameLogEntry entry\) ->[^)]*?)\bs\b", r"\1entry.plainText()", content)
        content = re.sub(r"\bs\.", "entry.plainText().", content)

    content, direct_changes = migrate_direct_calls(content)
    changes += direct_changes
    content = ensure_imports(content)

    if content != original:
        return content, max(changes, 1)
    return content, 0


def main() -> None:
    total = 0
    files = 0
    for path in sorted(TEST_SRC.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        migrated, changes = migrate_test_content(original)
        if changes:
            path.write_text(migrated, encoding="utf-8", newline="\n")
            total += changes
            files += 1
            print(f"{path.relative_to(ROOT)}: {changes}")
    print(f"Done. {total} changes in {files} test files.")


if __name__ == "__main__":
    main()
