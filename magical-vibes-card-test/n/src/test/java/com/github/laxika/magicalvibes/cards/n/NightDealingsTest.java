package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.o.OrochiHatchery;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightDealings.class, DevotedRetainer.class, GlacialRay.class, NezumiCutthroat.class,
        OrochiHatchery.class, Plains.class})
class NightDealingsTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to the opponent adds that many theft counters")
    void combatDamageAddsTheftCounters() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        Permanent attacker = addCreatureReady(player1, new NezumiCutthroat());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to the opponent adds that many theft counters")
    void noncombatDamageAddsTheftCounters() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to a creature, or to the controller, adds no theft counters")
    void onlyDamageToAnotherPlayerCounts() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NezumiCutthroat());
        harness.setHand(player1, List.of(new GlacialRay(), new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isZero();
    }

    @Test
    @DisplayName("Damage from an opponent does not add theft counters")
    void damageFromOpponentDoesNotCount() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
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
                .containsExactly("Nezumi Cutthroat");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();
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
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Nezumi Cutthroat");
    }

    @Test
    @DisplayName("X may be zero and can find a zero-mana nonland card")
    void zeroXSearchesForZeroManaNonlandCard() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        dealings.setCounterCount(CounterType.THEFT, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLibrary(player1, List.of(new OrochiHatchery(), new Plains()));

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Orochi Hatchery");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Orochi Hatchery");
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

    @Test
    @DisplayName("Each simultaneous combat damage source contributes its damage")
    void multipleCombatSourcesAddTheirDamage() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        addCreatureReady(player1, new NezumiCutthroat()).setAttacking(true);
        addCreatureReady(player1, new NezumiCutthroat()).setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(4);
    }

    @Test
    @DisplayName("Zero X is legal with no theft counters")
    void zeroXWithNoCounters() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLibrary(player1, List.of(new OrochiHatchery(), new Plains()));

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isZero();
        harness.assertInHand(player1, "Orochi Hatchery");
    }

    @Test
    @DisplayName("Counters are paid on activation and the chosen X survives counter changes")
    void countersArePaidBeforeResolution() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        dealings.setCounterCount(CounterType.THEFT, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null);

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isZero();
        dealings.setCounterCount(CounterType.THEFT, 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Nezumi Cutthroat");
        assertThat(dealings.getCounterCount(CounterType.THEFT)).isEqualTo(1);
    }

    @Test
    @DisplayName("A restricted search may fail to find an available card")
    void mayFailToFind() {
        Permanent dealings = harness.addToBattlefieldAndReturn(player1, new NightDealings());
        dealings.setCounterCount(CounterType.THEFT, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(dealings.getCounterCount(CounterType.THEFT)).isZero();
        harness.assertNotInHand(player1, "Nezumi Cutthroat");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new NezumiCutthroat(), new DevotedRetainer(), new Plains()));
    }
}
