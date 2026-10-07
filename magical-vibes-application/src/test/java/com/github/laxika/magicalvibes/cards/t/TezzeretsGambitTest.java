package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
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

@CardUsed({TezzeretsGambit.class, AlloyMyr.class})
class TezzeretsGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as a sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);

    }

    @Test
    @DisplayName("Resolving draws two cards and proliferates when permanents have counters")
    void drawsTwoAndProliferates() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlloyMyr());
        myr.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(myr.getId()));

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Resolving draws two cards when no permanents have counters (no proliferate choice needed)")
    void drawsTwoWhenNoEligiblePermanents() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Proliferate can choose none and still draws two cards")
    void proliferateChooseNoneStillDrawsTwo() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        myr.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Choose no permanents to proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(myr.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tezzeret's Gambit");
    }

    @Test
    @DisplayName("Proliferate adds -1/-1 counter to opponent's creature and draws two cards")
    void proliferateMinusCountersAndDrawsTwo() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        myr.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultiplePermanentsChosen(player1, List.of(myr.getId()));

        assertThat(myr.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Draws before choosing permanents and players to proliferate")
    void drawsBeforeProliferatingPermanentsAndPlayers() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new AlloyMyr());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounters = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 4);
        gd.playerEnergyCounters.put(player2.getId(), 2);

        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(withoutCounters.getCounters()).isEmpty();
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tezzeret's Gambit");
    }

    @Test
    @DisplayName("Can proliferate a player when no permanents have counters")
    void proliferatesPlayerWithoutEligiblePermanents() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Tezzeret's Gambit");
    }

    @Test
    @DisplayName("Can pay the Phyrexian blue symbol with two life")
    void paysPhyrexianManaWithLife() {
        harness.setHand(player1, List.of(new TezzeretsGambit()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Tezzeret's Gambit");
    }
}
