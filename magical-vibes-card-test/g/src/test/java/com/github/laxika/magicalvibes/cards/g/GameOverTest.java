package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GameOver.class, FountainOfYouth.class, GrizzlyBears.class})
class GameOverTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreaturesAndLeavesNoncreaturesAlone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());

        castGameOver("{3}{B}{B}");

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Costs two less when any player has half their starting life or less")
    void costsTwoLessWhenAnyPlayerHasHalfStartingLife() {
        harness.setLife(player2, 10);

        harness.castFromHand(player1, new GameOver(), "{1}{B}{B}");
    }

    @Test
    @DisplayName("Does not get the cost reduction when every player is above half their starting life")
    void doesNotGetCostReductionWhenEveryPlayerIsAboveHalfStartingLife() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new GameOver(), "{1}{B}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with a regeneration shield survives")
    void regenerationShieldSavesCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        castGameOver("{3}{B}{B}");

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller being at half starting life also reduces the cost")
    void controllerAtHalfStartingLifeReducesCost() {
        harness.setLife(player1, 10);

        castGameOver("{1}{B}{B}");

        harness.assertInGraveyard(player1, "Game Over");
    }

    @Test
    @DisplayName("Both players qualifying does not double the cost reduction")
    void bothPlayersQualifyingDoesNotDoubleReduction() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        assertThatThrownBy(() -> harness.castFromHand(player1, new GameOver(), "{B}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not remove either black mana requirement")
    void reductionLeavesBlackManaRequirements() {
        harness.setLife(player2, 10);

        assertThatThrownBy(() -> harness.castFromHand(player1, new GameOver(), "{2}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent at 20 life in Commander enables the reduction")
    void commanderOpponentAtHalfStartingLifeReducesCost() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 40);
        harness.setLife(player2, 20);

        castGameOver("{1}{B}{B}");

        harness.assertInGraveyard(player1, "Game Over");
    }

    @Test
    @DisplayName("The controller at 20 life in Commander enables the reduction")
    void commanderControllerAtHalfStartingLifeReducesCost() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 20);
        harness.setLife(player2, 40);

        castGameOver("{1}{B}{B}");

        harness.assertInGraveyard(player1, "Game Over");
    }

    @Test
    @DisplayName("Players above half starting life in Commander do not enable the reduction")
    void commanderAboveHalfStartingLifeDoesNotReduceCost() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 21);
        harness.setLife(player2, 21);

        assertThatThrownBy(() -> harness.castFromHand(player1, new GameOver(), "{1}{B}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGameOver(String manaCost) {
        harness.castFromHand(player1, new GameOver(), manaCost);
        harness.passBothPriorities();
    }
}
