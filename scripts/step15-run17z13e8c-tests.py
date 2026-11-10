#!/usr/bin/env python3
"""Step 15, run 17z13e8c: the rules-fix tests of the step readers' grouping (row 17z13e8c).

One triggered ability is one object on the stack (CR 603.3), and separate abilities are separate objects the
controller orders (CR 603.3b). Each case fails at `c1392804fa` and passes after this run:

- `PlaneMergeElfTest.landshipIsOneStackObject` (run 17z13e8b's probe): the your-upkeep reader put each untargeted
  effect on the stack as an object of its own, and the card wrote its one Landship ability as two `addTrigger` calls:
  2 entries, expected 1.
- `CustodyBattleTest.grantedAndPrintedUpkeepAbilitiesAreSeparate` (run 17z13e8b's probe): the your-upkeep reader
  gathered every player-target effect of a permanent into one choice, so Bloodgift Demon's printed and Custody Battle's
  granted abilities were one object with one target: 1 entry, expected 2.
- `MiragePhalanxTest.grantedAndPrintedCombatAbilitiesAreSeparate`: the beginning-of-combat reader merged every printed
  and statically granted ability of a permanent into one entry, so the "create a copy of this creature" ability Mirage
  Phalanx grants its partner and Mindwrack Harpy's printed "each player mills three cards" were one object: 2 entries
  (the Harpy's and Phalanx's own), expected 3.

Usage: step15-run17z13e8c-tests.py [REPOSITORY_ROOT]. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent)
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
    @CardUsed(com.github.laxika.magicalvibes.cards.b.BloodgiftDemon.class)
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
PHALANX = '''
    @Test
    @CardUsed(com.github.laxika.magicalvibes.cards.m.MindwrackHarpy.class)
    @DisplayName("The granted copy ability and the partner's printed combat ability are separate objects")
    void grantedAndPrintedCombatAbilitiesAreSeparate() {
        Permanent harpy = harness.addToBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.m.MindwrackHarpy());
        harness.setHand(player1, List.of(new MiragePhalanx()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harpy.getId());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Mindwrack Harpy"))
                .hasSize(2);
    }
'''
CASES = [
    ("p", "PlaneMergeElfTest", ELF, "landshipIsOneStackObject"),
    ("c", "CustodyBattleTest", CUSTODY, "grantedAndPrintedUpkeepAbilitiesAreSeparate"),
    ("m", "MiragePhalanxTest", PHALANX, "grantedAndPrintedCombatAbilitiesAreSeparate"),
]


def main():
    for letter, name, case, method in CASES:
        path = TESTS / letter / PKG / letter / f"{name}.java"
        text = path.read_text()
        if method in text:
            continue
        end = text.rstrip().rindex("}")
        path.write_text(text[:end].rstrip("\n") + "\n" + case + "}\n")
        print(f"added {path.relative_to(ROOT)}#{method}")


if __name__ == "__main__":
    main()
