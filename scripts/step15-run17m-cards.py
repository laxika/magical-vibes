#!/usr/bin/env python3
"""Step 15, run 17m: "Max speed — [ability]" is the ability's event condition.

"Max speed — [ability]" grants the ability while its controller has max speed: a condition on which the ability exists,
so `MaxSpeed` is the ability's event condition (`TriggerOptions.DEFAULT.withEventCondition(new MaxSpeed())`, the form
Endrider Spikespitter already has), judged as the ability triggers and not again as it resolves, where seven cards
wrote it as an intervening "if" (Aether Syphon, Embalmed Ascendant, Hangarback Assembler, Mendicant Core, Guidelight,
Rising Chicane, Smog Smasher (both abilities), Zahur, Glory's Past). The beginning-of-combat and draw collectors
judged it again by its class (the draw collector unwrapped it). Idempotent.

Usage: step15-run17m-cards.py <letter module dir>...
"""
import pathlib
import sys

CARDS = set(("AetherSyphon EmbalmedAscendant HangarbackAssembler MendicantCoreGuidelight RisingChicane SmogSmasher "
             "ZahurGlorysPast").split())


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            if path.stem not in CARDS:
                continue
            source = path.read_text()
            result = source.replace(".withInterveningIf(new MaxSpeed())", ".withEventCondition(new MaxSpeed())")
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
