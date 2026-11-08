#!/usr/bin/env python3
"""Step 15, run 17q: the step abilities' trigger-event intervening "if"s are event conditions.

Run 17m's scan (`step15-run17m-scan.py`) lists, under "EVENT", the step and phase abilities whose Oracle text has no
", if" right after the trigger condition but that the cards wrote with an intervening "if". Each one below is a
condition on which the ability exists, or a delayed trigger with no "if" at all, so it is the ability's event condition
(`withEventCondition`), judged as the ability triggers and not again as it resolves:

- a Class's level (CR 716.2a: "As long as this Class is level N or greater, it has [abilities]"): Blacksmith's Talent,
  Does Machines, Monk Class, Scavenger's Talent;
- "Solved — [ability]" (CR 719.3c): Case of the Shattered Pact's and Case of the Crimson Pulse's solved abilities;
- "∞ — [ability]" (CR 702.186b: "As long as this permanent is harnessed, it has [ability]"): The Mind Stone, The Soul
  Stone;
- a Room's door (CR 709.5: a locked half's rules text isn't the permanent's): Glassworks // Shattered Yard, Roaring
  Furnace // Steaming Sauna, Smoky Lounge // Misty Salon;
- "Whenever this creature attacks or blocks, remove a +1/+1 counter from it at end of combat" (a delayed trigger with
  no intervening "if"): Clockwork Dragon, Clockwork Vorrac.

The scan's other EVENT entries are real intervening "if"s, kept: vanishing's (CR 702.63a), impending's (CR 702.176a,
the Overlords) and a Case's "To solve" (CR 719.3a). Idempotent.

Usage: step15-run17q-cards.py <letter module dir>...
"""
import pathlib
import re
import sys

CONDITIONS = {
    "BlacksmithsTalent": "new AllOf(List.of(new SourceCounterThreshold(1, CounterType.LEVEL)))",
    "DoesMachines": "new SourceClassLevelAtLeast(3)",
    "MonkClass": "new SourceCounterThreshold(2, CounterType.LEVEL)",
    "ScavengersTalent": "new SourceClassLevelAtLeast(3)",
    "CaseOfTheShatteredPact": "new AllOf(List.of(new SourceIsSolved()))",
    "CaseOfTheCrimsonPulse": "new SourceIsSolved())",
    "TheMindStone": "new SourceIsHarnessed()",
    "TheSoulStone": "new SourceIsHarnessed()",
    "GlassworksShatteredYard": "new SourceRoomDoorUnlocked(1)",
    "RoaringFurnaceSteamingSauna": "new SourceRoomDoorUnlocked(1)",
    "SmokyLoungeMistySalon": "new SourceRoomDoorUnlocked(0)",
    "ClockworkDragon": "new SourceAttackedOrBlockedThisCombat()",
    "ClockworkVorrac": "new SourceAttackedOrBlockedThisCombat()",
}


def convert(source, condition):
    pattern = re.compile(r"\.withInterveningIf\((\s*)" + re.escape(condition))
    converted = re.compile(r"\.withEventCondition\(\s*" + re.escape(condition))
    found = len(pattern.findall(source))
    if found == 0:
        if not converted.search(source):
            raise SystemExit(f"condition not found: {condition}")
        return source
    if found != 1:
        raise SystemExit(f"{found} abilities with {condition}")
    return pattern.sub(lambda m: ".withEventCondition(" + m.group(1) + condition, source)


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            condition = CONDITIONS.get(path.stem)
            if condition is None:
                continue
            source = path.read_text()
            result = convert(source, condition)
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
