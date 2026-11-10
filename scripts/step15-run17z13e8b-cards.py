#!/usr/bin/env python3
"""Step 15, run 17z13e8b: the cards whose draw abilities the draw reader judged by an effect's draw count.

"Whenever you draw your second card each turn" is the trigger's count (`Triggers.youDrawSecondCardEachTurn()`, CR 121.1
counts the cards drawn this turn), not the effect's `controllerDrawCount` that the controller-draw reader judged through
`CardEffect.triggersOnControllerDrawCount` (gone, with `PutCountersOnSourceEffect.onSecondControllerDraw` and
`SequenceEffect.onSecondControllerDraw`):

- Lat Nam Adept, Thopter Mechanic and Gurgling Anointer put a +1/+1 counter on themselves
  (`PutCountersOnSourceEffect(1, 1, 1)`);
- Gixian Puppeteer's "each opponent loses 2 life and you gain 2 life" is one ability of two effects (CR 603.3): the draw
  readers put an ability on the stack whole.

Idempotent; fails if an edit didn't apply. Usage: step15-run17z13e8b-cards.py [LETTER...] (card modules: g, l, t; none
given: all of them).
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "com/github/laxika/magicalvibes/cards"

COUNTER_OLD = "addTrigger(Triggers.youDrawCard(), PutCountersOnSourceEffect.onSecondControllerDraw());"
COUNTER_NEW = "addTrigger(Triggers.youDrawSecondCardEachTurn(), new PutCountersOnSourceEffect(1, 1, 1));"
GIXIAN_OLD = re.compile(
    r"addTrigger\(Triggers\.youDrawCard\(\), SequenceEffect\.onSecondControllerDraw\(\s*"
    r"new LoseLifeEffect\(2, LoseLifeRecipient\.EACH_OPPONENT\),\s*new GainLifeEffect\(2\)\)\);")
GIXIAN_NEW = ("addTrigger(Triggers.youDrawSecondCardEachTurn(),\n"
              "                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),\n"
              "                new GainLifeEffect(2));")
SEQUENCE_IMPORT = "import com.github.laxika.magicalvibes.model.effect.SequenceEffect;\n"


def counter(text):
    return text.replace(COUNTER_OLD, COUNTER_NEW)


def gixian(text):
    text = GIXIAN_OLD.sub(GIXIAN_NEW, text)
    body = text.replace(SEQUENCE_IMPORT, "")
    return body if not re.search(r"\bSequenceEffect\b", body) else text


EDITS = [
    ("l", "LatNamAdept", counter, COUNTER_NEW),
    ("t", "ThopterMechanic", counter, COUNTER_NEW),
    ("g", "GurglingAnointer", counter, COUNTER_NEW),
    ("g", "GixianPuppeteer", gixian, GIXIAN_NEW),
]


def main():
    letters = set(sys.argv[1:])
    for letter, name, edit, check in EDITS:
        if letters and letter not in letters:
            continue
        path = CARDS / letter / "src/main/java" / PKG / letter / f"{name}.java"
        text = path.read_text()
        new = edit(text)
        if check not in new or "onSecondControllerDraw" in new:
            raise SystemExit(f"{name}: the edit did not apply")
        if new != text:
            path.write_text(new)
            print(f"edited {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
