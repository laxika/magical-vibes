#!/usr/bin/env python3
"""Step 15, run 17l: the card tests the run's collector changes touch.

- Screeching Silcaw: its metalcraft ability takes the common "deals combat damage to a player" path, which logs the
  trigger as every other one does; the case asserted "metalcraft ability triggers", the text of the removed
  class-keyed branch. It now asserts that the ability's trigger was logged, and still that the player milled four.

Idempotent.

Usage: step15-run17l-tests.py <letter test module dir>...
"""
import pathlib
import sys

SILCAW_BEFORE = '''        assertThat(gameLogContains("metalcraft ability triggers")).isTrue();'''
SILCAW_AFTER = '''        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText())
                .contains("Screeching Silcaw's").contains("trigger"));'''


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/test/java/**/ScreechingSilcawTest.java")):
            source = path.read_text()
            if SILCAW_BEFORE in source:
                path.write_text(source.replace(SILCAW_BEFORE, SILCAW_AFTER))
                changed.append(path.stem)
    print(f"{len(changed)} test classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
