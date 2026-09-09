package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({StrawGolem.class, BenalishInfantry.class, MindStone.class, SteelGolem.class})
class StrawGolemTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a creature spell sacrifices Straw Golem")
    void opponentCastingCreatureSpellSacrificesGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        opponentCastsCreatureSpell(new BenalishInfantry(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Straw Golem");
        harness.assertInGraveyard(player1, "Straw Golem");
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not sacrifice Straw Golem")
    void opponentCastingNoncreatureSpellDoesNotSacrificeGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MindStone(), "{2}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Straw Golem");
    }

    @Test
    @DisplayName("Controller casting a creature spell does not sacrifice Straw Golem")
    void controllerCastingCreatureSpellDoesNotSacrificeGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new BenalishInfantry(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Straw Golem");
    }

    @Test
    @DisplayName("An opponent casting an artifact creature spell sacrifices Straw Golem")
    void opponentCastingArtifactCreatureSpellSacrificesGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        opponentCastsCreatureSpell(new SteelGolem(), "{3}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Straw Golem");
        harness.assertInGraveyard(player1, "Straw Golem");
    }

    private void opponentCastsCreatureSpell(Card creature, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, creature, manaCost);
    }
}
