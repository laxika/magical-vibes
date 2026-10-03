package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
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

@CardUsed({BenevolentHydra.class, GrizzlyBears.class, TimberlandGuide.class})
class BenevolentHydraTest extends BaseCardTest {

    @Test
    @DisplayName("enters with X +1/+1 counters without adding one to itself")
    void entersWithXCountersWithoutSelfReplacement() {
        harness.setHand(player1, List.of(new BenevolentHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Benevolent Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("adds one counter when another controlled creature gets +1/+1 counters")
    void addsOneCounterToAnotherControlledCreature() {
        harness.addToBattlefield(player1, new BenevolentHydra());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("removes a counter and adds two to another target creature")
    void activatedAbilityMovesCounterToAnotherCreature() {
        Permanent hydra = addCreatureReady(player1, new BenevolentHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hydra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("cannot target itself with its activated ability")
    void activatedAbilityCannotTargetItself() {
        Permanent hydra = addCreatureReady(player1, new BenevolentHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hydra.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hydra.isTapped()).isFalse();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anotherHydraAddsOneToEntryCountersAsABatch() {
        harness.addToBattlefield(player1, new BenevolentHydra());
        harness.setHand(player1, List.of(new BenevolentHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Benevolent Hydra").get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void zeroEntryCountersRemainZeroWithAnotherHydra() {
        harness.addToBattlefield(player1, new BenevolentHydra());
        harness.setHand(player1, List.of(new BenevolentHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Benevolent Hydra").get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingHydraDoesNotIncreaseEntryCounters() {
        harness.addToBattlefield(player2, new BenevolentHydra());
        harness.setHand(player1, List.of(new BenevolentHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Benevolent Hydra")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void multipleHydrasEachAddOneButRecipientDoesNotAddToItself() {
        Permanent source = addCreatureReady(player1, new BenevolentHydra());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new BenevolentHydra());
        harness.addToBattlefield(player1, new BenevolentHydra());

        harness.activateAbility(player1, 0, null, recipient.getId());

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void counterPlacementOnSourceIsNotIncreased() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BenevolentHydra());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, hydra.getId());
        resolveAllTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterPlacementOnOpposingCreatureIsNotIncreased() {
        harness.addToBattlefield(player1, new BenevolentHydra());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BenevolentHydra());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, opponent.getId());
        resolveAllTriggers();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityCannotTargetOpposingCreature() {
        Permanent source = addCreatureReady(player1, new BenevolentHydra());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BenevolentHydra());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityRequiresCounterToRemove() {
        Permanent source = addCreatureReady(player1, new BenevolentHydra());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new BenevolentHydra());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, recipient.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void summoningSickHydraCannotActivateTapAbility() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BenevolentHydra());
        source.setSummoningSick(true);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new BenevolentHydra());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, recipient.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
