package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShivanSandMage.class, AncestralVision.class, GrizzlyBears.class})
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
    void removesTwoTimeCountersFromTargetSuspendedCard() {
        AncestralVision target = suspendedCard(4);

        castAtTriggerTime(0, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 2);
    }

    @Test
    void putsTwoTimeCountersOnTargetSuspendedCard() {
        AncestralVision target = suspendedCard(1);

        castAtTriggerTime(1, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
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
        AncestralVision target = suspendedCard(1);

        castAtTriggerTime(0, target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent permanentWithTimeCounters(com.github.laxika.magicalvibes.model.Player player, int count) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setCounterCount(CounterType.TIME, count);
        return permanent;
    }

    private AncestralVision suspendedCard(int timeCounters) {
        AncestralVision card = new AncestralVision();
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
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();
    }
}
