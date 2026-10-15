package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BiogenicUpgrade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sharktocrab.class, SauroformHybrid.class, StonyStrength.class, BiogenicUpgrade.class})
class SharktocrabTest extends BaseCardTest {

    @Test
    void adaptingPutsCounterTapsOpponentCreatureAndSkipsItsNextUntap() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        Permanent ownCreature = addCreatureReady(player1, new SauroformHybrid());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void adaptCanBeActivatedWithCountersButDoesNothingOnResolution() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        sharktocrab.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void twoAdaptActivationsCheckCountersAtResolution() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        addAdaptMana();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void adaptStillAddsCounterWhenThereIsNoLegalTriggerTarget() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void counterFromAnotherSpellLocksAlreadyTappedCreatureAfterSourceLeaves() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        opponentCreature.tap();
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, sharktocrab.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sharktocrab);
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(opponentCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void multipleCountersTriggerOncePerPlacementAndLocksDoNotAccumulate() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new BiogenicUpgrade()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, Map.of(sharktocrab.getId(), 3));
        harness.passBothPriorities();
        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void adaptIgnoresOtherCounterTypesAndCanAdaptAgainAfterCountersAreRemoved() {
        Permanent sharktocrab = addCreatureReady(player1, new Sharktocrab());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        sharktocrab.setCounterCount(CounterType.CHARGE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();
        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sharktocrab.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        sharktocrab.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addAdaptMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(sharktocrab.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void triggerDoesNothingIfTargetIsNoLongerAnOpponentsCreature() {
        addCreatureReady(player1, new Sharktocrab());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerBattlefields.get(player1.getId()).add(opponentCreature);
        resolveAllTriggers();

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
