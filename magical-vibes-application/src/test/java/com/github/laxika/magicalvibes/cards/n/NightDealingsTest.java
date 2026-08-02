package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightDealingsTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to the opponent adds that many theft counters")
    void combatDamageAddsTheftCounters() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        addCreatureReady(player1, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to the opponent adds that many theft counters")
    void noncombatDamageAddsTheftCounters() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to a creature, or to the controller, adds no theft counters")
    void onlyDamageToAnotherPlayerCounts() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isZero();
    }

    @Test
    @DisplayName("Removing X theft counters searches for a nonland card with mana value X")
    void removesCountersAndSearchesForManaValueX() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        dealings.setCounterCount(CounterType.THEFT, 5);
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The chosen card goes to hand")
    void chosenCardGoesToHand() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        dealings.setCounterCount(CounterType.THEFT, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("X may not exceed the theft counters on the enchantment")
    void cannotRemoveMoreCountersThanPresent() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        dealings.setCounterCount(CounterType.THEFT, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new LlanowarElves(), new Plains()));
    }
}
