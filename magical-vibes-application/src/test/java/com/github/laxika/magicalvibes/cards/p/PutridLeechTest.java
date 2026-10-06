package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutridLeech.class})
class PutridLeechTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life grants +2/+2 until end of turn")
    void payTwoLifeGrantsBoost() {
        Permanent leech = addReadyLeech(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second activation in the same turn is rejected")
    void secondActivationSameTurnRejected() {
        addReadyLeech(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Cannot activate with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        addReadyLeech(player1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent leech = addReadyLeech(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyLeech(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Life is paid immediately and the activation limit applies before resolution")
    void lifePaidAndLimitConsumedBeforeResolution() {
        Permanent leech = addReadyLeech(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.assertLife(player1, 18);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Leech can activate on the opponent's turn")
    void activatesWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent leech = harness.addToBattlefieldAndReturn(player1, new PutridLeech());
        leech.setSummoningSick(true);
        leech.tap();
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(4);
        assertThat(leech.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each Leech has its own once-per-turn limit and boosts only itself")
    void activationLimitsArePerPermanent() {
        Permanent first = addReadyLeech(player1);
        Permanent second = addReadyLeech(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private Permanent addReadyLeech(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PutridLeech());
        perm.setSummoningSick(false);
        return perm;
    }
}
