#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 7: Confusion in the Ranks' rules-fix card test.

Idempotent; run on the letter test module:

    python3 scripts/step15-enters-rebinding-tests.py magical-vibes-card-test/c

"Shares a card type with it" now judges the entering permanent by its current characteristics (CR 608.2h): under
March of the Machines an entering noncreature artifact is an artifact creature, so a creature another player controls
is a legal target.
"""
import pathlib
import sys

TEST = '''
    @Test
    @DisplayName("The entering permanent's current types count: under March of the Machines an artifact shares creature")
    void enteringPermanentIsJudgedByItsCurrentTypes() {
        harness.addToBattlefield(player1, new ConfusionInTheRanks());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Atog());
        harness.setHand(player1, List.of(new AetherSpellbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Atog");
        harness.assertOnBattlefield(player2, "Aether Spellbomb");
    }
}
'''


def main(module):
    for path in sorted(pathlib.Path(module).rglob("ConfusionInTheRanksTest.java")):
        src = path.read_text()
        if "enteringPermanentIsJudgedByItsCurrentTypes" in src:
            continue
        converted = src.replace("import com.github.laxika.magicalvibes.cards.i.Island;\n",
                                "import com.github.laxika.magicalvibes.cards.i.Island;\n"
                                "import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;\n", 1)
        converted = converted.replace("SeatOfTheSynod.class, TrollAscetic.class, Island.class})",
                                      "SeatOfTheSynod.class, TrollAscetic.class, Island.class, MarchOfTheMachines.class})", 1)
        converted = converted.rstrip()
        assert converted.endswith("}")
        converted = converted[:-1].rstrip() + "\n" + TEST
        path.write_text(converted)
        print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1])
