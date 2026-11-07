#!/usr/bin/env python3
"""Step 15, run 17k: the card tests of the run's rules fixes.

- Squall, Gunblade Duelist: "if any of those creatures have power or toughness equal to the chosen number" is an
  intervening "if" (CR 603.4), now checked again as the ability resolves, where the collector unwrapped it by its
  condition's class. The new case changes the matching attacker's power and toughness while the ability is on the
  stack.

Idempotent.

Usage: step15-run17k-tests.py <letter test module dir>...
"""
import pathlib
import sys

SQUALL_CASE = '''
    @Test
    void conditionIsCheckedAgainAsTheAbilityResolves() {
        Permanent squall = addCreatureReady(player1, new SquallGunbladeDuelist());
        squall.setChosenNumber(2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        int lifeBefore = gd.getLife(player2.getId());
        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        bears.setPowerModifier(1);
        bears.setToughnessModifier(1);
        resolveAllTriggers();

        // No creature attacking player2 has power or toughness 2 as the ability resolves: only the Bears' combat damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }
'''

CASES = {"SquallGunbladeDuelistTest": ("conditionIsCheckedAgainAsTheAbilityResolves", SQUALL_CASE)}


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/test/java/**/*.java")):
            if path.stem not in CASES:
                continue
            method, case = CASES[path.stem]
            source = path.read_text()
            if f"void {method}()" in source:
                continue
            end = source.rstrip().rindex("}")
            path.write_text(source[:end].rstrip() + "\n" + case + "}\n")
            changed.append(path.stem)
    print(f"{len(changed)} test classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
