#!/usr/bin/env python3
"""Step 15, run 17z14: the cards with two or more triggered abilities on one trigger other than a step's (row 17z14h).

One Oracle ability is one `addTrigger` (CR 603.3): an ability split into several calls is several stack objects once
its reader reads each ability whole, and joined again only where a reader still merges abilities. Run 17z13e8c
converted the step triggers' cards and run 17z14 the planes' chaos abilities; this lists the rest, per card, with the
trigger written twice or more, to read against the Oracle text (many print separate abilities: evoke, myriad,
firebending, a Saga's "I, II" written per chapter is one ability per chapter).

Read-only. Usage: step15-run17z14-scan.py

`DONE` names the cards a run converted (its own `step15-run<RUN>-cards.py`), `SEPARATE` the cards a run read against
their Oracle text and kept apart because their abilities on that trigger really are separate; neither is listed again.
"""
import collections
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"

# Run 17z14b (scripts/step15-run17z14b-cards.py): one Oracle ability each, now one addTrigger.
DONE = {
    "Archipelagore", "SnapdaxApexOfTheHunt", "PolukranosWorldEater", "ShipbreakerKraken", "WildfireCerberus",
    "RoilingHorror", "SirenOfTheSilentSong", "ScouringSwarm", "Sharktocrab", "KrosDefenseContractor",
    "ObeliskSpider", "AuntieOolCursewretch", "ObNixilisUnshackled", "CoolButRude",
    # Run 17z14c (scripts/step15-run17z14c-cards.py): the self-cycled collector reads each ability whole.
    "YidaroWanderingMonster", "MangarasBlessing",
}
# Run 17z14b: read against their Oracle text; their abilities on the listed trigger print apart (Class levels,
# vanishing beside its own ability, two cast abilities).
SEPARATE = {"AdvancedFloralInvocations", "GourmandsTalent", "RegenerationsRestored", "JeskaiAscendancy"}
STEP = re.compile(r"Triggers\.(upkeep|drawStep|endStep|beginningOfCombat|precombatMainPhase|postcombatMainPhase"
                  r"|secondMainPhase|endOfCombat)\b")


def first_argument(text, start):
    """The first top-level argument of the call whose '(' is at `start`."""
    depth, quoted, current = 0, False, ""
    for index in range(start, len(text)):
        char = text[index]
        if quoted:
            current += char
            if char == '"' and text[index - 1] != "\\":
                quoted = False
            continue
        if char == '"':
            quoted = True
            current += char
        elif char in "([{":
            depth += 1
            if depth > 1:
                current += char
        elif char in ")]}":
            depth -= 1
            if depth == 0:
                return current.strip()
            current += char
        elif char == "," and depth == 1:
            return current.strip()
        else:
            current += char
    return current.strip()


def main():
    rows = []
    for path in sorted(ROOT.rglob("*.java")):
        text = path.read_text()
        counts = collections.Counter()
        for match in re.finditer(r"addTrigger\(", text):
            counts[re.sub(r"\s+", " ", first_argument(text, match.end() - 1))] += 1
        for trigger, count in counts.items():
            if count >= 2 and not STEP.search(trigger) and path.stem not in DONE | SEPARATE:
                rows.append((path.stem, count, trigger))
    for name, count, trigger in rows:
        print(f"{name} {count} {trigger}")
    print(f"{len(rows)} triggers, {len({row[0] for row in rows})} cards")


if __name__ == "__main__":
    main()
