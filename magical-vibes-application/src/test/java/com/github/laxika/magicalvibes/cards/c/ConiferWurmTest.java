package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConiferWurm.class, SnowCoveredForest.class})
class ConiferWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X based on snow permanents its controller controls")
    void getsBoostBasedOnSnowPermanentsYouControl() {
        Permanent wurm = addReadyWurm();
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.getEffectivePower()).isEqualTo(7);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent wurm = addReadyWurm();
        addSnowPermanent(player1);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wurm.getEffectivePower()).isEqualTo(4);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts snow permanents at resolution and keeps that boost afterward")
    void countsAtResolutionAndKeepsBoostFixed() {
        Permanent wurm = addReadyWurm();
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        addSnowPermanent(player1);
        harness.passBothPriorities();
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(6);
        addSnowPermanent(player1);
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Repeated activations stack and do not require tapping or haste")
    void repeatedActivationsWhileSummoningSickAndTapped() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new ConiferWurm());
        wurm.setSummoningSick(true);
        wurm.setTapped(true);
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wurm.getEffectivePower()).isEqualTo(5);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(5);
        addSnowPermanent(player1);
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wurm.getEffectivePower()).isEqualTo(7);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(7);
        assertThat(wurm.isTapped()).isTrue();
    }

    private Permanent addReadyWurm() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new ConiferWurm());
        wurm.setSummoningSick(false);
        return wurm;
    }

    private void addSnowPermanent(Player player) {
        harness.addToBattlefield(player, new SnowCoveredForest());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
