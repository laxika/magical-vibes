package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.r.RiftBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Clockspinning.class, AncestralVision.class, AshcoatBear.class, RiftBolt.class})
class ClockspinningTest extends BaseCardTest {

    @Test
    void addsCounterToTargetPermanent() {
        Permanent target = permanentWithCounter();
        cast(target.getId());

        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removesCounterFromTargetPermanent() {
        Permanent target = permanentWithCounter();
        cast(target.getId());

        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void adjustsTheChosenCounterTypeWhenTargetHasMultipleCounterKinds() {
        Permanent target = permanentWithCounter();
        target.setCounterCount(CounterType.TIME, 1);
        cast(target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void adjustsTimeCounterOnSuspendedCard() {
        AncestralVision target = suspendedCard(2);
        cast(target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void removingLastTimeCounterOffersSuspendCast() {
        AncestralVision target = suspendedCard(1);
        cast(target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void buybackReturnsClockspinningToHand() {
        Permanent target = permanentWithCounter();
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Clockspinning");
    }

    @Test
    void withoutBuybackPutsClockspinningInGraveyard() {
        Permanent target = permanentWithCounter();
        cast(target.getId());

        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "ADD");

        harness.assertInGraveyard(player1, "Clockspinning");
    }

    @Test
    void cannotTargetAnUnsuspendedExiledCard() {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    void counterlessPermanentIsLegalAndBuybackStillReturnsSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Clockspinning");
        harness.assertNotInGraveyard(player1, "Clockspinning");
    }

    @Test
    void buybackDoesNotReturnSpellWhenTargetLeavesBattlefield() {
        Permanent target = permanentWithCounter();
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Clockspinning");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void counterTypeIsChosenFromCountersPresentAtResolution() {
        Permanent target = permanentWithCounter();
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        target.setCounterCount(CounterType.TIME, 2);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Clockspinning");
    }

    @Test
    void removingTimeCounterWithoutRemovingLastDoesNotOfferSuspendCast() {
        AncestralVision target = suspendedCard(2);
        cast(target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Clockspinning");
    }

    @Test
    void permanentLosingAllCountersBeforeResolutionDoesNothing() {
        Permanent target = permanentWithCounter();
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Clockspinning");
    }

    @Test
    void cannotTargetExiledCardWithNonSuspendTimeCounters() {
        AshcoatBear target = new AshcoatBear();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 2);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    void canTargetCardWithSuspendEvenWhenAnotherEffectPlacedItsTimeCounters() {
        RiftBolt target = new RiftBolt();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        cast(target.getId());
        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 4);
        harness.assertInGraveyard(player1, "Clockspinning");
    }

    private Permanent permanentWithCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return target;
    }

    private AncestralVision suspendedCard(int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
