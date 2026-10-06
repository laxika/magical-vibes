package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipperyBogbonder.class, GrizzlyBears.class, Forest.class, HardenedScales.class})
class SlipperyBogbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a hexproof counter on the target and moves chosen counter kinds onto it")
    void putsHexproofCounterAndMovesAnyCounterKinds() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 2);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.CHARGE, 3);

        castBogbonder(target);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not prompt when no other controlled creature has counters")
    void doesNotPromptWithoutCountersToMove() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBogbonder(target);

        assertThat(target.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only creatures are legal targets")
    void rejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SlipperyBogbonder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can decline moving counters while still receiving hexproof")
    void canMoveZeroCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 3);

        castBogbonder(target);
        harness.handleListChoice(player1, "0");

        assertThat(target.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can move only some counters onto an opponent's newly hexproof creature")
    void movesPartialAmountOntoOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 3);

        castBogbonder(target);
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Counters already on the target cannot be moved onto itself")
    void doesNotOfferTargetsOwnCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castBogbonder(target);

        assertThat(target.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Moving counters from multiple creatures applies Hardened Scales only once")
    void movesCountersFromMultipleSourcesAsOnePlacement() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstSource.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        secondSource.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castBogbonder(target);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "2");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(target.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(firstSource.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondSource.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castBogbonder(Permanent target) {
        harness.setHand(player1, List.of(new SlipperyBogbonder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
