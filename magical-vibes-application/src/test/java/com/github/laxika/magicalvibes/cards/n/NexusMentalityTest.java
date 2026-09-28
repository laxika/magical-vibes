package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NexusMentality.class, EdgarMarkov.class, Forest.class, GrizzlyBears.class})
class NexusMentalityTest extends BaseCardTest {

    @Test
    void firstModeMovesAllCountersBetweenNonlandPermanents() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        source.setCounterCount(CounterType.CHARGE, 1);
        destination.setCounterCount(CounterType.LOYALTY, 3);

        cast(new int[]{0}, List.of(source.getId(), destination.getId()));

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void secondModeRemovesAllCountersAndDrawsThatManyCards() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(target.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void commanderAllowsBothModes() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        Permanent drawTarget = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        drawTarget.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        cast(new int[]{0, 1}, List.of(source.getId(), destination.getId(), drawTarget.getId()));

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(drawTarget.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void bothModesRequireControllingTheRegisteredCommander() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void modesCannotTargetLands() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
