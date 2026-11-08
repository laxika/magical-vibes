#!/usr/bin/env python3
"""Step 15, run 17u: the "done" audit's rebuild scan.

Lists every construction of an effect class (other than `MayEffect`, `ConditionalEffect`, `SequenceEffect` and
`ChooseOneEffect`) in the trigger, combat and turn packages, `DrawService` and `TriggeredAbilityQueueService`, whose
arguments (the rest of the line and the next two) read the trigger event, a record, an amount or an id. Each hit is a
candidate rebuild with bound event data; the run's Findings classify them. Read-only.

Usage: step15-run17u-audit.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
E = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
FILES = (list((E / "trigger").glob("*.java")) + list((E / "trigger/pipeline").glob("*.java"))
         + list((E / "combat").rglob("*.java")) + list((E / "turn").glob("*.java"))
         + [E / "DrawService.java", E / "TriggeredAbilityQueueService.java"])
SKIP = {"MayEffect", "ConditionalEffect", "SequenceEffect", "ChooseOneEffect"}
CONSTRUCTION = re.compile(r"new ([A-Z][A-Za-z]*Effect)\(")
EVENT_READ = re.compile(r"\b(epd|dc|sc|ctx|death|dying\w*|trigger|effect|e|may|mustBlock|context|match|spell\w*"
                        r"|attacker|creature|perm|source\w*|entering\w*|tapped\w*|exiled\w*|target\w*)\.")
AMOUNT = re.compile(r"(Power|Toughness|ManaValue|Id\(\)|amount|damage|count)")


def main():
    for path in sorted(FILES):
        lines = path.read_text().split("\n")
        for i, line in enumerate(lines):
            for match in CONSTRUCTION.finditer(line):
                if match.group(1) in SKIP:
                    continue
                args = (line[match.end():] + " " + " ".join(lines[i + 1:i + 3]))[:220]
                if EVENT_READ.search(args) or AMOUNT.search(args):
                    print(f"{path.relative_to(E)}:{i + 1}: {match.group(1)}: {line.strip()[:170]}")


if __name__ == "__main__":
    main()
