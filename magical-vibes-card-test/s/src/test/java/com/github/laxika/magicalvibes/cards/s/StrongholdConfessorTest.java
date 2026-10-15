package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({StrongholdConfessor.class})
class StrongholdConfessorTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without kicker enters with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent confessor = findPermanent(player1, "Stronghold Confessor");
        assertThat(confessor).isNotNull();
        assertThat(confessor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast with kicker enters with two +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent confessor = findPermanent(player1, "Stronghold Confessor");
        assertThat(confessor).isNotNull();
        assertThat(confessor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cast with kicker enters with two +1/+1 counters")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2); // only 2 generic (need 3 for kicker)

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining kicker remains legal even with enough mana to pay it")
    void canDeclineAffordableKicker() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Stronghold Confessor")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new StrongholdConfessor());
        addCreatureReady(player2, new StrongholdConfessor());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new StrongholdConfessor());
        Permanent first = addCreatureReady(player2, new StrongholdConfessor());
        Permanent second = addCreatureReady(player2, new StrongholdConfessor());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
