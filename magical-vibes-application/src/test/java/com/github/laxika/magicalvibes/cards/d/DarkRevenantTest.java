package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkRevenant.class, Cremate.class, PullFromEternity.class})
class DarkRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("When Dark Revenant dies, its trigger puts it from the graveyard on top of its owner's library")
    void diesThenTriggerPutsItOnTopOfLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, new DarkRevenant());
        // Mark lethal damage on the 2/2 and let state-based actions destroy it.
        revenant.setMarkedDamage(2);

        harness.runStateBasedActions();

        // It first enters the graveyard, then its death trigger waits on the stack.
        harness.assertInGraveyard(player1, "Dark Revenant");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Dark Revenant");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Dark Revenant");
    }

    @Test
    @DisplayName("Death trigger puts only the dying Revenant above the existing library")
    void putsDyingCardAboveExistingLibrary() {
        DarkRevenant libraryCard = new DarkRevenant();
        DarkRevenant graveyardCard = new DarkRevenant();
        DarkRevenant dyingCard = new DarkRevenant();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, dyingCard);
        revenant.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dyingCard, libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("A Revenant controlled by an opponent goes to its owner's library")
    void returnsToOwnersLibraryUnderOpposingControl() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        DarkRevenant card = new DarkRevenant();
        card.setOwnerId(player1.getId());
        Permanent revenant = harness.addToBattlefieldAndReturn(player2, card);
        revenant.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Dark Revenant");
    }

    @Test
    @DisplayName("Death trigger does not retrieve a Revenant exiled in response")
    void doesNotRetrieveCardExiledInResponse() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new DarkRevenant()));
        DarkRevenant card = new DarkRevenant();
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player2, List.of(new Cremate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        revenant.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.castAndResolveInstant(player2, 0, card.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Dark Revenant");
    }

    @Test
    @DisplayName("An old death trigger cannot find a Revenant that left and reentered the graveyard")
    void doesNotRetrieveNewGraveyardIncarnation() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new DarkRevenant()));
        DarkRevenant card = new DarkRevenant();
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.setHand(player2, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        revenant.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.castAndResolveInstant(player2, 0, card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);

        harness.castAndResolveInstant(player1, 0, card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
