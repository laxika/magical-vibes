package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadyProgress.class, CarapaceForger.class})
class SteadyProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SteadyProgress.class);
    }

    @Test
    @DisplayName("Resolving proliferates and draws a card when permanents have counters")
    void proliferatesAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        // Proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Resolving draws a card when no permanents have counters (no proliferate choice needed)")
    void drawsWhenNoEligiblePermanents() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Proliferate can choose none and still draws a card")
    void proliferateChooseNoneStillDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        // Choose no permanents to proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Steady Progress");
    }

    @Test
    @DisplayName("Proliferate adds -1/-1 counter to opponent's creature and draws a card")
    void proliferateMinusCountersAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Proliferate adds each counter kind only to chosen permanents and players before drawing")
    void proliferatesMultipleKindsOnChosenPermanentsAndPlayers() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 4);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        harness.assertInGraveyard(player1, "Steady Progress");
    }

    @Test
    @DisplayName("Proliferate can choose a player when no permanents have counters")
    void proliferatesPlayerWithoutEligiblePermanents() {
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
