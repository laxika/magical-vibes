package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BringLow;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoodedHydra.class, BringLow.class, HardenedScales.class})
class HoodedHydraTest extends BaseCardTest {

    @Test
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void putsFiveCountersOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hydra));

        assertThat(hydra.isFaceDown()).isFalse();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void createsOneGreenSnakeForEachPlusOneCounterWhenItDies() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new HoodedHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player2, List.of(new BringLow()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        for (Permanent snake : findPermanents(player1, "Snake")) {
            assertThat(snake.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
        }
    }

    @Test
    void castingWithZeroXDiesWithoutCreatingSnakes() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hooded Hydra");
        harness.assertInGraveyard(player1, "Hooded Hydra");
        assertThat(findPermanents(player1, "Snake")).isEmpty();
    }

    @Test
    void dyingFaceDownDoesNotCreateSnakesEvenWithCounters() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.isFaceDown()).isTrue();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player2, List.of(new BringLow()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveInstant(player2, 0, hydra.getId());

        harness.assertInGraveyard(player1, "Hooded Hydra");
        assertThat(findPermanents(player1, "Snake")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpAddsCountersImmediatelyToExistingCounters() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hydra));

        assertThat(hydra.isFaceDown()).isFalse();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hooded Hydra");
    }

    @Test
    void deathUsesLastKnownPlusOneCountersAndIgnoresOtherCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new HoodedHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        hydra.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, hydra.getId());
        harness.assertInGraveyard(player2, "Hooded Hydra");
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).isEmpty();
        assertThat(findPermanents(player2, "Snake")).hasSize(3);
        for (Permanent snake : findPermanents(player2, "Snake")) {
            assertThat(snake.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(1);
        }
    }

    @Test
    void entryCountersApplyCounterReplacementEffects() {
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void turnFaceUpCountersApplyCounterReplacementEffects() {
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hydra));

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }
}
