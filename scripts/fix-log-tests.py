#!/usr/bin/env python3
"""Fix remaining test compile issues after GameLogEntry migration."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEST_SRC = ROOT / "magical-vibes-application" / "src" / "test" / "java"
GAME_LOG_IMPORT = "import com.github.laxika.magicalvibes.model.GameLog;"
GAME_LOG_ENTRY_IMPORT = "import com.github.laxika.magicalvibes.model.GameLogEntry;"


def ensure_imports(content: str) -> str:
    if "GameLog." in content and GAME_LOG_IMPORT not in content:
        content = re.sub(r"(package [^;]+;\n)", r"\1" + GAME_LOG_IMPORT + "\n", content, count=1)
    if "GameLogEntry" in content and GAME_LOG_ENTRY_IMPORT not in content:
        if GAME_LOG_IMPORT in content:
            content = content.replace(
                GAME_LOG_IMPORT + "\n",
                GAME_LOG_IMPORT + "\n" + GAME_LOG_ENTRY_IMPORT + "\n",
                1,
            )
        else:
            content = re.sub(r"(package [^;]+;\n)", r"\1" + GAME_LOG_ENTRY_IMPORT + "\n", content, count=1)
    return content


def migrate(content: str) -> tuple[str, int]:
    original = content
    changes = 0

    content, n = re.subn(
        r'logAndBroadcast\((eq\([^)]+\)), eq\("((?:\\.|[^"\\])*)"\)\)',
        r'logAndBroadcast(\1, eq(GameLog.text("\2")))',
        content,
    )
    changes += n

    content, n = re.subn(
        r'logAndBroadcast\((eq\([^)]+\)),\s*\n\s*eq\("((?:\\.|[^"\\])*)"\)\)',
        r'logAndBroadcast(\1, eq(GameLog.text("\2")))',
        content,
    )
    changes += n

    for prefix in ("contains", "org.mockito.ArgumentMatchers.contains"):
        pattern = rf'logAndBroadcast\((eq\([^)]+\)), {prefix}\("([^"]*)"\)\)'
        content, n = re.subn(
            pattern,
            r'logAndBroadcast(\1, argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("\2")))',
            content,
        )
        changes += n

    content, n = re.subn(
        r"argThat\(\(GameLogEntry entry\) ->",
        r"argThat((GameLogEntry logEntry) ->",
        content,
    )
    changes += n

    if "argThat((GameLogEntry logEntry) ->" in content:
        content = content.replace("entry.plainText()", "logEntry.plainText()")

    content = ensure_imports(content)
    if content != original:
        return content, max(changes, 1)
    return content, 0


def main() -> None:
    total = 0
    files = 0
    for path in sorted(TEST_SRC.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        migrated, changes = migrate(original)
        if changes:
            path.write_text(migrated, encoding="utf-8", newline="\n")
            total += changes
            files += 1
            print(f"{path.relative_to(ROOT)}: {changes}")
    print(f"Done. {total} changes in {files} files.")


if __name__ == "__main__":
    main()
