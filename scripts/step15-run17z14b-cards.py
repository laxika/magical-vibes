#!/usr/bin/env python3
"""Step 15, run 17z14b: the cards that wrote one Oracle ability as several `addTrigger` calls on a watch whose
collectors now dispatch each ability whole (row 17z14b; row 17z14h's list, `step15-run17z14-scan.py`).

The registry hands a watch's handler one triggered ability, its effects together, and the generic collectors dispatch
each ability once (CR 603.3), so an ability split into several `addTrigger` calls is several stack objects: the
monstrous, time-counter-in-exile, self-untap and mutate collectors no longer join a permanent's abilities into one
object, and the counter, library-search, level and sacrifice collectors read each ability as its own. Each card below
was read against its Oracle text and prints one ability there; its calls become one `addTrigger` with every effect,
in order (a chained `target(...).addTrigger(...)` keeps its target group for every effect).

Usage: step15-run17z14b-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"


def chained(trigger):
    """`target(...).addTrigger(T, a).addTrigger(T, b)` -> `target(...).addTrigger(T, a, b)`."""
    return re.compile(r"\)\s*\n(\s*)\.addTrigger\(" + trigger + r",\s*")


def statements(trigger):
    """`addTrigger(T, a);\n addTrigger(T, b);` -> `addTrigger(T, a,\n b);`."""
    return re.compile(r"\);\s*\n(\s*)addTrigger\(" + trigger + r",\s*")


MONSTROUS = r"Triggers\.becomesMonstrous\(\)"
MUTATES = r"Triggers\.mutates\(\)"

# card -> (pattern, trigger whose calls must end as one)
EDITS = {
    "Archipelagore": (chained(MUTATES), MUTATES),
    "SnapdaxApexOfTheHunt": (chained(MUTATES), MUTATES),
    "PolukranosWorldEater": (chained(MONSTROUS), MONSTROUS),
    "ShipbreakerKraken": (chained(MONSTROUS), MONSTROUS),
    "WildfireCerberus": (statements(MONSTROUS), MONSTROUS),
    "RoilingHorror": (statements(r"Triggers\.timeCounterRemovedFromThisInExile\(\)"),
                      r"Triggers\.timeCounterRemovedFromThisInExile\(\)"),
    "SirenOfTheSilentSong": (statements(r"Triggers\.becomesUntapped\(\)"), r"Triggers\.becomesUntapped\(\)"),
    "ScouringSwarm": (statements(r"Triggers\.sacrificed\(sacrificedLand, ObjectRelation\.YOU_CONTROL\)"),
                      r"Triggers\.sacrificed\(sacrificedLand, ObjectRelation\.YOU_CONTROL\)"),
    "Sharktocrab": (chained(r"Triggers\.plusOneCountersPutOnThis\(\)"), r"Triggers\.plusOneCountersPutOnThis\(\)"),
    "KrosDefenseContractor": (statements(r"Triggers\.youPutCountersOnCreatureYouDontControl\(\)"),
                              r"Triggers\.youPutCountersOnCreatureYouDontControl\(\)"),
    "ObeliskSpider": (statements(r"Triggers\.youPutMinusOneCountersOnCreature\(\)"),
                      r"Triggers\.youPutMinusOneCountersOnCreature\(\)"),
    "AuntieOolCursewretch": (statements(r"Triggers\.minusOneCountersPutOnCreature\(\)"),
                             r"Triggers\.minusOneCountersPutOnCreature\(\)"),
    "ObNixilisUnshackled": (statements(r"Triggers\.opponentSearchesTheirLibrary\(\)"),
                            r"Triggers\.opponentSearchesTheirLibrary\(\)"),
    "CoolButRude": (statements(r"Triggers\.reachesLevelThree\(\)"), r"Triggers\.reachesLevelThree\(\)"),
}


def find(name):
    for module in ROOT.iterdir():
        if not (module / PKG).is_dir():
            continue
        for path in (module / PKG).rglob(f"{name}.java"):
            return module.name, path
    raise SystemExit(f"{name}: no class found")


def main():
    letters = set(sys.argv[1:])
    for name, (pattern, trigger) in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        if len(re.findall(r"addTrigger\(\s*" + trigger, text)) == 1:
            continue
        first = re.search(r"addTrigger\(\s*" + trigger, text).start()
        parts, count, last = [], 0, 0
        for match in pattern.finditer(text):
            if match.start() < first:
                continue
            parts.append(text[last:match.start()])
            parts.append(",\n" + match.group(1) + "        ")
            last = match.end()
            count += 1
        parts.append(text[last:])
        new = "".join(parts)
        if count == 0 or len(re.findall(r"addTrigger\(\s*" + trigger, new)) != 1:
            raise SystemExit(f"{name}: edit didn't apply")
        # Snapdax's chained call starts on its target line, so its effects sit eight columns further left.
        new = new.replace(",\n                        new GainLifeEffect(4));", ",\n                new GainLifeEffect(4));")
        path.write_text(new)
        print(f"merged {module}/{name}")


if __name__ == "__main__":
    main()
