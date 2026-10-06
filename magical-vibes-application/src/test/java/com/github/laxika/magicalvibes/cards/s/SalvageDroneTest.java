package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalvageDrone.class, Forest.class, GrizzlyBears.class, Shock.class})
class SalvageDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent drone = addCreatureReady(player1, new SalvageDrone());
        drone.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveUnblockedCombat();

        ExiledCardEntry exiledCard = gd.findExiledCard(topCard.getId());
        assertThat(exiledCard).isNotNull();
        assertThat(exiledCard.sourcePermanentId()).isNull();
    }

    @Test
    @DisplayName("When Salvage Drone dies, accepting its trigger draws then discards")
    void deathTriggerDrawsThenDiscardsWhenAccepted() {
        Permanent drone = addCreatureReady(player1, new SalvageDrone());
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, drone.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining Salvage Drone's death trigger does nothing")
    void deathTriggerCanBeDeclined() {
        Permanent drone = addCreatureReady(player1, new SalvageDrone());
        Card topCard = new Forest();
        Card handCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock(), handCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, drone.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Ingest exiles only the top card and leaves the controller's library alone")
    void ingestExilesExactlyOneCard() {
        Permanent drone = addCreatureReady(player1, new SalvageDrone());
        drone.setAttacking(true);
        Card topCard = new Forest();
        Card secondCard = new Forest();
        Card ownCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, secondCard));
        harness.setLibrary(player1, List.of(ownCard));

        resolveUnblockedCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(secondCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Ingest against an empty library does not cause a loss")
    void ingestWithEmptyLibrary() {
        Permanent drone = addCreatureReady(player1, new SalvageDrone());
        drone.setAttacking(true);
        harness.setLibrary(player2, List.of());

        resolveUnblockedCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The card drawn by the death trigger can itself be discarded")
    void canDiscardTheNewlyDrawnCard() {
        Permanent drone = addCreatureReady(player1, new SalvageDrone());
        Card drawnCard = new Forest();
        Card keptCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Shock(), keptCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, drone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveUnblockedCombat() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
