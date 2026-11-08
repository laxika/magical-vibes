#!/usr/bin/env python3
"""Step 15, run 17x: the card tests of the storm family's conversion.

- The commander-cast copy tests (Echo Storm, Skull Storm, Genesis Storm, Fury Storm, Empyrial Storm, Hatut Zeraze
  Strike Force) modelled "you've cast your commander N times" by setting the commander's tax alone, which the deleted
  `CopyThisSpellForEachCommanderCastEffect` read. The count is now `CommanderCastsFromCommandZoneThisGame`, the casts
  the engine records per caster (the amount every other commander-cast card reads), so each test also records the
  casts its tax stands for.

- Super Combo's two replicate cases answered a "choose new targets" prompt for each copy. Its copies are now made by
  the shared controller-copy machinery, which, for a spell with two targets, asks about each target only when it has
  another legal target; in these cases neither has one (one creature on each side), so the copies go straight onto the
  stack and the cases assert that instead.

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17x-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

COMMANDER_TESTS = {
    "EchoStormTest": "e", "SkullStormTest": "s", "GenesisStormTest": "g", "FuryStormTest": "f",
    "EmpyrialStormTest": "e", "HatutZerazeStrikeForceTest": "h",
}

TAX = re.compile(r"^(?P<indent> *)gd\.commanderTaxByCardId\.put\(commander\.getId\(\), (?P<tax>\d+)\);\n"
                 r"(?!(?P=indent)gd\.recordCommanderCastFromCommandZone)", re.M)


def record_casts(match):
    indent = match.group("indent")
    casts = int(match.group("tax")) // 2
    return (match.group(0)
            + f"{indent}gd.recordCommanderCastFromCommandZone(player1.getId());\n" * casts)


SUPER_COMBO = [
    ("""        castSuperCombo(source, target, List.of("{2}", "{2}"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
""",
     """        castSuperCombo(source, target, List.of("{2}", "{2}"));

        harness.passBothPriorities();
        resolveAllTriggers();
"""),
    ("""        harness.passBothPriorities();

        assertThat(gd.stagedSpellCopies).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
""",
     """        harness.passBothPriorities();

        // Neither target has another legal choice, so no new-target prompt holds the copies off the stack.
        assertThat(gd.stagedSpellCopies).isEmpty();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
"""),
]


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, letter in COMMANDER_TESTS.items():
        if letter not in letters:
            continue
        path = ROOT / (f"magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/"
                       f"{letter}/{name}.java")
        text = path.read_text()
        converted = TAX.sub(record_casts, text)
        if converted != text:
            path.write_text(converted)
            print(f"converted {name}")
    if "s" in letters:
        path = ROOT / "magical-vibes-card-test/s/src/test/java/com/github/laxika/magicalvibes/cards/s/SuperComboTest.java"
        text = path.read_text()
        converted = text
        for old, new in SUPER_COMBO:
            if new not in converted:
                if old not in converted:
                    raise SystemExit(f"SuperComboTest: expected text not found:\n{old}")
                converted = converted.replace(old, new)
        if converted != text:
            path.write_text(converted)
            print("converted SuperComboTest")


if __name__ == "__main__":
    main()
