#!/usr/bin/env python3
"""Wrap logAndBroadcast string arguments with GameLog.text()."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ENGINE_SRC = ROOT / "magical-vibes-engine" / "src" / "main" / "java"
GAME_LOG_IMPORT = "import com.github.laxika.magicalvibes.model.GameLog;"

METHOD_PREFIX = "logAndBroadcast("
GAME_DATA_FIRST_ARG = re.compile(r"^(?:gameData|.+\.gameData\(\))$")


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
    if stripped.startswith("GameLogEntry."):
        return True
    if stripped.startswith("GameLog.builder("):
        return True
    if stripped.startswith("GameLog.entersBattlefield"):
        return True
    if stripped.startswith("GameLog.playerChoosesForCard("):
        return True
    if stripped.startswith("GameLog.text("):
        return True
    if stripped.endswith(".build())") and "Builder" in stripped:
        return True
    if stripped.endswith(".build()") and "logBuilder" in stripped:
        return True
    return False


def ensure_game_log_import(content: str) -> str:
    if GAME_LOG_IMPORT in content:
        return content
    if "GameLog." not in content:
        return content
    match = re.search(r"(package [^;]+;\s*\n)", content)
    if not match:
        return content
    insert_at = match.end()
    rest = content[insert_at:]
    import_block = re.match(r"((?:import [^;]+;\s*\n)*)", rest)
    if import_block:
        imports = import_block.group(1)
        if "import com.github.laxika.magicalvibes.model.GameData;" in imports:
            return (
                content[:insert_at]
                + imports.replace(
                    "import com.github.laxika.magicalvibes.model.GameData;",
                    "import com.github.laxika.magicalvibes.model.GameData;\n" + GAME_LOG_IMPORT,
                    1,
                )
                + content[insert_at + len(imports) :]
            )
        return content[:insert_at] + imports + GAME_LOG_IMPORT + "\n" + content[insert_at + len(imports) :]
    return content[:insert_at] + GAME_LOG_IMPORT + "\n" + content[insert_at:]


def migrate_content(content: str) -> tuple[str, int]:
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
    if changes:
        content = ensure_game_log_import(content)
    return content, changes


def main() -> None:
    total_changes = 0
    files_changed = 0
    for path in sorted(ENGINE_SRC.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        migrated, changes = migrate_content(original)
        if changes:
            path.write_text(migrated, encoding="utf-8", newline="\n")
            total_changes += changes
            files_changed += 1
            print(f"{path.relative_to(ROOT)}: {changes}")
    print(f"Done. {total_changes} call sites in {files_changed} files.")


if __name__ == "__main__":
    main()
