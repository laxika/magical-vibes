package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiftElemental.class, AncestralVision.class, GrizzlyBears.class})
class RiftElementalTest extends BaseCardTest {

    @Test
    void removesTimeCounterFromControlledPermanentAndBoostsSelf() {
        Permanent source = readySource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        addActivationMana();

        activate(source);

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    void removesTimeCounterFromOwnedSuspendedCard() {
        Permanent source = readySource();
        AncestralVision target = suspendedCard(player1, 2);
        addActivationMana();

        activate(source);

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
        assertThat(source.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void choosesBetweenControlledPermanentAndSuspendedCard() {
        Permanent source = readySource();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        permanent.setCounterCount(CounterType.TIME, 1);
        AncestralVision suspended = suspendedCard(player1, 2);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(source), null, null);

        PendingInteraction.RemoveTimeCounterCostChoice choice =
                (PendingInteraction.RemoveTimeCounterCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(permanent.getCard().getId(), suspended.getId());
        harness.handleMultipleCardsChosen(player1, List.of(suspended.getId()));
        harness.passBothPriorities();

        assertThat(permanent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 1);
        assertThat(source.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void removingLastSuspendedTimeCounterOffersItsCast() {
        Permanent source = readySource();
        AncestralVision target = suspendedCard(player1, 1);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(source), null, null);

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void cannotActivateWithoutAnEligibleTimeCounter() {
        Permanent source = readySource();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(source), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("time counter");
    }

    private Permanent readySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RiftElemental());
        source.setSummoningSick(false);
        return source;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void activate(Permanent source) {
        harness.activateAbility(player1, battlefieldIndex(source), null, null);
        harness.passBothPriorities();
    }

    private AncestralVision suspendedCard(Player owner, int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(owner, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
