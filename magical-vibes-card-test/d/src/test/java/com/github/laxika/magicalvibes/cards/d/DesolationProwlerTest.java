package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesolationProwler.class})
class DesolationProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life gives Desolation Prowler +2/+2 until end of turn")
    void payingLifeBoostsSelf() {
        Permanent prowler = addCreatureReady(player1, new DesolationProwler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addCreatureReady(player1, new DesolationProwler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent prowler = addCreatureReady(player1, new DesolationProwler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Life is paid before resolution and the activation limit applies while the ability is on the stack")
    void paymentAndLimitApplyBeforeResolution() {
        Permanent prowler = addCreatureReady(player1, new DesolationProwler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Insufficient life prevents activation without consuming the turn's use")
    void insufficientLifeDoesNotConsumeActivation() {
        Permanent prowler = addCreatureReady(player1, new DesolationProwler());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();

        harness.setLife(player1, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Prowler can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new DesolationProwler());
        prowler.setSummoningSick(true);
        prowler.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(prowler.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Prowler has its own activation limit and boosts only itself")
    void separateCopiesHaveIndependentLimits() {
        Permanent first = addCreatureReady(player1, new DesolationProwler());
        Permanent second = addCreatureReady(player1, new DesolationProwler());
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
    }

    @Test
    @DisplayName("The ability can be used again during the opponent's next turn")
    void activationLimitResetsOnOpponentsTurn() {
        Permanent prowler = addCreatureReady(player1, new DesolationProwler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }
}
