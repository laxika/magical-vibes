#!/usr/bin/env python3
"""Step 15, run 17z13e8b: probe cases for row 17z13e8c (the step readers' ability grouping), not kept in the tree.

Run 17z13e8b read the step readers against "one triggered ability is one stack object of its declared effects"
(CR 603.3, 603.3b) and confirmed two violations through the engine; these cases fail at `e7301f7e84` and after run
17z13e8b, and are the row's rules-fix tests once the readers put each ability on the stack whole:

- `PlaneMergeElfTest.landshipIsOneStackObject`: the your-upkeep reader puts each untargeted effect on the stack as an
  object of its own, so Plane-Merge Elf's one Landship ability (written as two `addTrigger` calls, its look and its
  "if it's a land, you may reveal it") is two stack objects: 2 entries, expected 1.
- `CustodyBattleTest.grantedAndPrintedUpkeepAbilitiesAreSeparate`: the your-upkeep reader gathers every player-target
  effect of a permanent into one `UpkeepPlayerTargetTrigger`, so Bloodgift Demon's printed "target player draws a card
  and loses 1 life" and Custody Battle's granted "target opponent gains control of this creature unless you sacrifice a
  land" are one stack object with one target (Custody Battle's "target opponent" filter): 1 entry, expected 2.

Usage: step15-run17z13e8b-probes.py [--revert] [REPOSITORY_ROOT]. Idempotent either way.
"""
import pathlib
import sys

ROOT = pathlib.Path(next((a for a in sys.argv[1:] if not a.startswith("--")),
                         pathlib.Path(__file__).resolve().parent.parent))
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

ELF = '''
    @Test
    @DisplayName("Landship is one ability: one object on the stack")
    void landshipIsOneStackObject() {
        harness.addToBattlefield(player1, new PlaneMergeElf());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
    }
'''
CUSTODY = '''
    @Test
    @DisplayName("The creature's printed and granted upkeep abilities are two objects with their own targets")
    void grantedAndPrintedUpkeepAbilitiesAreSeparate() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.b.BloodgiftDemon());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new CustodyBattle());
        aura.setAttachedTo(demon.getId());

        advanceToUpkeep(player1);
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }

        assertThat(gd.stack).hasSize(2);
    }
'''
PROBES = [
    ("p", "PlaneMergeElfTest", ELF, "landshipIsOneStackObject"),
    ("c", "CustodyBattleTest", CUSTODY, "grantedAndPrintedUpkeepAbilitiesAreSeparate"),
]


def main():
    revert = "--revert" in sys.argv
    for letter, name, case, method in PROBES:
        path = TESTS / letter / PKG / letter / f"{name}.java"
        text = path.read_text()
        if revert:
            new = text.replace(case, "")
        elif method in text:
            new = text
        else:
            end = text.rstrip().rindex("}")
            new = text[:end].rstrip("\n") + "\n" + case + "}\n"
        if new != text:
            path.write_text(new)
            print(f"{'reverted' if revert else 'applied'} {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
