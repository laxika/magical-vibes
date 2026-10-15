package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fervor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderdarkBeholder.class, Shock.class, Forest.class, GrizzlyBears.class, Divination.class,
        Fervor.class, Murder.class})
class UnderdarkBeholderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with ten eyestalk counters")
    void entersWithEyestalkCounters() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());

        assertThat(beholder.getCounterCount(CounterType.EYEBALL)).isEqualTo(10);
    }

    @Test
    @DisplayName("Replaces damage by removing eyestalk counters")
    void removesEyestalkCountersInsteadOfTakingDamage() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player2, new UnderdarkBeholder());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, beholder.getId());
        harness.passBothPriorities();

        assertThat(beholder.getCounterCount(CounterType.EYEBALL)).isEqualTo(8);
        assertThat(beholder.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Underdark Beholder");
    }

    @Test
    @DisplayName("Sacrifices itself when damage exceeds its eyestalk counters")
    void sacrificesWhenDamageExceedsEyestalkCounters() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player2, new UnderdarkBeholder());
        beholder.setCounterCount(CounterType.EYEBALL, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, beholder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Underdark Beholder");
        assertThat(beholder.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking reveals until a qualifying spell and casts it for free")
    void attackingRevealsAndCastsQualifyingSpell() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());
        beholder.setSummoningSick(false);
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        Card spell = new Divination();
        harness.setLibrary(player1, List.of(land, creature, spell));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(spell);
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).contains(land, creature);
    }

    @Test
    void damageEqualToCountersLeavesBeholderAliveWithNoCounters() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player2, new UnderdarkBeholder());
        beholder.setCounterCount(CounterType.EYEBALL, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, beholder.getId());
        harness.passBothPriorities();

        assertThat(beholder.getCounterCount(CounterType.EYEBALL)).isZero();
        assertThat(beholder.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Underdark Beholder");
    }

    @Test
    void attackingCanCastAnEnchantmentWithoutMana() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());
        beholder.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Fervor()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fervor");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manaValueMustBeStrictlyLessThanCurrentCounterCount() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());
        beholder.setSummoningSick(false);
        beholder.setCounterCount(CounterType.EYEBALL, 3);
        Card equalManaValue = new Divination();
        Card lowerManaValue = new Shock();
        harness.setLibrary(player1, List.of(equalManaValue, lowerManaValue));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(lowerManaValue);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(equalManaValue, lowerManaValue);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningFreeCastReturnsAllRevealedCardsToLibrary() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());
        beholder.setSummoningSick(false);
        Card land = new Forest();
        Card spell = new Divination();
        harness.setLibrary(player1, List.of(land, spell));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void noQualifyingCardReturnsEntireLibraryWithoutOfferingCast() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());
        beholder.setSummoningSick(false);
        beholder.setCounterCount(CounterType.EYEBALL, 0);
        Card land = new Forest();
        Card spell = new Divination();
        harness.setLibrary(player1, List.of(land, spell));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, spell);
    }

    @Test
    void attackTriggerUsesLastKnownCountersAfterBeholderIsDestroyed() {
        Permanent beholder = harness.enterBattlefieldAndReturn(player1, new UnderdarkBeholder());
        beholder.setSummoningSick(false);
        Card spell = new Fervor();
        harness.setLibrary(player1, List.of(spell));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, beholder.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Underdark Beholder");
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(spell);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fervor");
    }
}
