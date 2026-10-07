package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @DisplayName("The sacrifice trigger resolves before the opponent's creature spell")
    void sacrificeResolvesBeforeCreatureSpell() {
        harness.addToBattlefield(player1, new StrawGolem());

        opponentCastsCreatureSpell(new BenalishInfantry(), "{2}{W}");

        harness.assertOnBattlefield(player1, "Straw Golem");
        harness.assertNotOnBattlefield(player2, "Benalish Infantry");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Straw Golem");
        harness.assertNotOnBattlefield(player2, "Benalish Infantry");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Benalish Infantry");
    }

    @Test
    @DisplayName("An opponent's creature entering without being cast does not trigger sacrifice")
    void creatureEnteringWithoutCastingDoesNotSacrificeGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        harness.enterBattlefieldAndReturn(player2, new BenalishInfantry());

        harness.assertOnBattlefield(player1, "Straw Golem");
        harness.assertOnBattlefield(player2, "Benalish Infantry");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Straw Golem sacrifices itself when an opponent casts a creature")
    void multipleGolemsEachSacrificeThemselves() {
        harness.addToBattlefield(player1, new StrawGolem());
        harness.addToBattlefield(player1, new StrawGolem());

        opponentCastsCreatureSpell(new BenalishInfantry(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Straw Golem");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Straw Golem"))
                .hasSize(2);
    }

    private void opponentCastsCreatureSpell(Card creature, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, creature, manaCost);
    }
}
