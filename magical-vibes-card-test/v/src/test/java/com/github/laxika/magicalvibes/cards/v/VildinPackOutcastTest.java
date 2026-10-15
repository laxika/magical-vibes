package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DronepackKindred;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VildinPackOutcast.class})
class VildinPackOutcastTest extends BaseCardTest {

    @Test
    void redAbilityBoostsPowerAndReducesToughnessUntilEndOfTurn() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 1);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(3);
    }

    @Test
    void cannotTransformWithoutSevenManaIncludingTwoRed() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, outcast), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(outcast.isTransformed()).isFalse();
    }

    @Test
    void transformsAndBackFaceAbilityBoostsPower() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, outcast), 1, null, null);
        harness.passBothPriorities();

        assertThat(outcast.isTransformed()).isTrue();
        assertThat(outcast.getCard()).isInstanceOf(DronepackKindred.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(7);
    }

    @Test
    void frontFaceBoostsStackAndExpireAtCleanup() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 2);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(4);
    }

    @Test
    void reducingToughnessToZeroPutsOutcastIntoGraveyard() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 4);
        forceMainPhase(player1);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Vildin-Pack Outcast");
        harness.assertInGraveyard(player1, "Vildin-Pack Outcast");
    }

    @Test
    void frontFaceBoostPersistsThroughTransformAndBothBoostsExpireAtCleanup() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, outcast), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(6);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, outcast), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(outcast.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(7);
    }

    @Test
    void twoPendingTransformActivationsDoNotTransformBackToFront() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new VildinPackOutcast());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, outcast), 1, null, null);
        harness.activateAbility(player1, indexOf(player1, outcast), 1, null, null);
        resolveAllTriggers();

        assertThat(outcast.isTransformed()).isTrue();
        assertThat(outcast.getCard()).isInstanceOf(DronepackKindred.class);
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
