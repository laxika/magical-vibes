package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonstrosityOfTheLake.class, GrizzlyBears.class, Island.class, Forest.class})
class MonstrosityOfTheLakeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {5} taps and stuns all creatures controlled by opponents")
    void payingFiveTapsAndStunsOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrosityOfTheLake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the entry payment leaves creatures unchanged")
    void decliningEntryPaymentDoesNothing() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrosityOfTheLake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Islandcycling searches for an Island")
    void islandcyclingSearchesForIsland() {
        harness.setHand(player1, List.of(new MonstrosityOfTheLake()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monstrosity of the Lake");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Island");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Already tapped creatures receive another stun counter, but noncreatures do not")
    void alreadyTappedCreaturesReceiveStunCounters() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedCreature.tap();
        tappedCreature.setCounterCount(CounterType.STUN, 1);
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MonstrosityOfTheLake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(tappedCreature.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(untappedCreature.isTapped()).isTrue();
        assertThat(untappedCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(opponentLand.isTapped()).isFalse();
        assertThat(opponentLand.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(tappedCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(untappedCreature.isTapped()).isTrue();
        assertThat(untappedCreature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(tappedCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(untappedCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Islandcycling discards immediately and may fail to find an available Island")
    void islandcyclingCanFailToFind() {
        harness.setHand(player1, List.of(new MonstrosityOfTheLake()));
        harness.setLibrary(player1, List.of(new Island(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Monstrosity of the Lake");
        harness.assertNotInHand(player1, "Monstrosity of the Lake");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Island", "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Islandcycling without an Island leaves the library cards in the library")
    void islandcyclingWithoutMatchingCard() {
        harness.setHand(player1, List.of(new MonstrosityOfTheLake()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monstrosity of the Lake");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
