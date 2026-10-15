package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.p.Petrify;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandMoonsInfantry.class, ArmoredKincaller.class, Petrify.class})
class ThousandMoonsInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps itself during each other player's untap step")
    void untapsItselfDuringOpponentsUntapStep() {
        Permanent infantry = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent other = addCreatureReady(player1, new ArmoredKincaller());

        infantry.tap();
        other.tap();

        advanceToNextTurn(player1);

        assertThat(infantry.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap another permanent during an opponent's untap step")
    void doesNotUntapOtherPermanents() {
        Permanent infantry = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent other = addCreatureReady(player1, new ArmoredKincaller());

        infantry.tap();
        other.tap();

        advanceToNextTurn(player1);

        assertThat(infantry.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a newly entered Infantry without clearing its summoning sickness")
    void untapsNewlyEnteredInfantry() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new ThousandMoonsInfantry());
        infantry.setSummoningSick(true);
        infantry.tap();

        advanceToNextTurn(player1);

        assertThat(infantry.isTapped()).isFalse();
        assertThat(infantry.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Each copy untaps itself without untapping unrelated creatures")
    void untapsMultipleCopies() {
        Permanent first = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent second = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent other = addCreatureReady(player1, new ArmoredKincaller());
        first.tap();
        second.tap();
        other.tap();

        advanceToNextTurn(player1);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Works for the other controller while the active player's permanents untap normally")
    void worksForEitherController() {
        Permanent infantry = addCreatureReady(player2, new ThousandMoonsInfantry());
        Permanent other = addCreatureReady(player2, new ArmoredKincaller());
        Permanent activeCreature = addCreatureReady(player1, new ArmoredKincaller());
        infantry.tap();
        other.tap();
        activeCreature.tap();

        advanceToNextTurn(player2);

        assertThat(infantry.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(activeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Remains tapped outside untap steps, then untaps on its controller's turn")
    void untapsOnlyDuringUntapSteps() {
        Permanent infantry = addCreatureReady(player1, new ThousandMoonsInfantry());
        infantry.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(infantry.isTapped()).isTrue();

        advanceToNextTurn(player2);

        assertThat(infantry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Petrify does not prevent the static untap ability")
    void untapsWhileEnchantedByPetrify() {
        Permanent infantry = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Petrify());
        aura.setAttachedTo(infantry.getId());
        infantry.tap();

        advanceToNextTurn(player1);

        assertThat(infantry.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextPlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
