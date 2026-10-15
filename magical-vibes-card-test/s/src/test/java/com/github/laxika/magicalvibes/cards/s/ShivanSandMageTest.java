package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.k.KnightOfSursi;
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

@CardUsed({ShivanSandMage.class, KnightOfSursi.class, BlindPhantasm.class})
class ShivanSandMageTest extends BaseCardTest {

    @Test
    void removesTwoTimeCountersFromTargetPermanent() {
        Permanent target = permanentWithTimeCounters(player2, 3);

        cast(0, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void putsTwoTimeCountersOnTargetPermanentWithTimeCounter() {
        Permanent target = permanentWithTimeCounters(player2, 1);

        cast(1, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void cannotPutTimeCountersOnTargetPermanentWithoutTimeCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());

        assertThatThrownBy(() -> cast(1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removesTwoTimeCountersFromTargetSuspendedCard() {
        KnightOfSursi target = suspendedCard(4);

        castAtTriggerTime(0, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 2);
    }

    @Test
    void putsTwoTimeCountersOnTargetSuspendedCard() {
        KnightOfSursi target = suspendedCard(1);

        castAtTriggerTime(1, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void cannotTargetAnUnsuspendedExiledCard() {
        KnightOfSursi target = new KnightOfSursi();
        harness.setExile(player2, List.of(target));

        assertThatThrownBy(() -> cast(0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void suspendExilesShivanSandMageWithFourTimeCounters() {
        ShivanSandMage card = new ShivanSandMage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void removingLastTimeCounterFromTargetSuspendedCardOffersItsSuspendCast() {
        KnightOfSursi target = suspendedCard(1);

        castAtTriggerTime(0, target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void removalCanTargetPermanentWithoutTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());

        cast(0, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void removesOnlyAvailableTimeCounterFromPermanent() {
        Permanent target = permanentWithTimeCounters(player2, 1);

        cast(0, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void removalCannotTargetExiledCardWithTimeCountersButWithoutSuspend() {
        BlindPhantasm target = new BlindPhantasm();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        assertThatThrownBy(() -> castAtTriggerTime(0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void additionCannotTargetExiledCardWithTimeCountersButWithoutSuspend() {
        BlindPhantasm target = new BlindPhantasm();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        assertThatThrownBy(() -> castAtTriggerTime(1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    private Permanent permanentWithTimeCounters(com.github.laxika.magicalvibes.model.Player player, int count) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BlindPhantasm());
        permanent.setCounterCount(CounterType.TIME, count);
        return permanent;
    }

    private KnightOfSursi suspendedCard(int timeCounters) {
        KnightOfSursi card = new KnightOfSursi();
        harness.setExile(player2, List.of(card));
        gd.exiledCardTimeCounters.put(card.getId(), timeCounters);
        return card;
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShivanSandMage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, mode, targetId);
        resolveAllTriggers();
    }

    private void castAtTriggerTime(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShivanSandMage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(targetId));
        resolveAllTriggers();
    }
}
