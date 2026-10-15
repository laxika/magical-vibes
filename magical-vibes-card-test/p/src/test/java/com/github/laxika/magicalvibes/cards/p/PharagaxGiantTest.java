package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FatedConflagration;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PharagaxGiant.class, FatedConflagration.class})
class PharagaxGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Paying tribute puts two +1/+1 counters on Pharagax Giant and deals no damage")
    void tributePaid() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent giant = castGiant();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining tribute deals 5 damage to each opponent")
    void tributeNotPaidDealsDamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent giant = castGiant();

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The damage trigger resolves even if Pharagax Giant dies in response")
    void damageTriggerSurvivesSourceRemoval() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent giant = castGiant();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        harness.setHand(player2, java.util.List.of(new FatedConflagration()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pharagax Giant");
        harness.assertNotOnBattlefield(player1, "Pharagax Giant");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Tribute is chosen by the opponent and damage follows the Giant's controller")
    void tributeUnderOtherController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new PharagaxGiant(), "{4}{R}");
        harness.passBothPriorities();
        Permanent giant = findPermanent(player2, "Pharagax Giant");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    private Permanent castGiant() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PharagaxGiant(), "{4}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Pharagax Giant");
    }
}
