#!/usr/bin/env python3
"""Step 15 run 1: card classes stop passing a card id the trigger collectors used to bind.

`new RegisterDelayedReturnCardFromGraveyardToHandEffect(null)` becomes the no-arg constructor: the card to
return is the stack entry's triggering card, or the source card when the ability has none. Run per letter
module (`--letter t`) or over every module (no argument); rerunnable.
"""
import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REWRITES = [
    (re.compile(r"new RegisterDelayedReturnCardFromGraveyardToHandEffect\(\s*null\s*\)"),
     "new RegisterDelayedReturnCardFromGraveyardToHandEffect()"),
    (re.compile(r"(// at the beginning of the next end step\.) The collector binds the dying card's id\."),
     r"\1 The card is the stack entry's triggering card."),
    (re.compile(r"The null cardId makes the delayed return use the discarded card\."),
     "The delayed return takes the discarded card, the stack entry's triggering card."),
]

parser = argparse.ArgumentParser()
parser.add_argument("--letter", help="the card module's letter, e.g. t; all modules when omitted")
args = parser.parse_args()
modules = [ROOT / "magical-vibes-card" / args.letter] if args.letter else sorted(
    p for p in (ROOT / "magical-vibes-card").iterdir() if p.is_dir())
changed = 0
for module in modules:
    for path in (module / "src").rglob("*.java"):
        text = path.read_text()
        new = text
        for pattern, replacement in REWRITES:
            new = pattern.sub(replacement, new)
        if new != text:
            path.write_text(new)
            changed += 1
            print(path.relative_to(ROOT))
print(f"{changed} card classes changed")
