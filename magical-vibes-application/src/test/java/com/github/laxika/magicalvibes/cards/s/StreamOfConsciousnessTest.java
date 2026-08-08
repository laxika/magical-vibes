package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StreamOfConsciousnessTest extends BaseCardTest {

    private void castStream(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new StreamOfConsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Shuffles the chosen cards from the target player's graveyard into their library")
    void shufflesChosenCardsIntoLibrary() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castStream(player1.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Stream of Consciousness");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);
    }

    @Test
    @DisplayName("Up to four cards may be chosen even with more cards in the graveyard")
    void maxTargetsCappedAtFour() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears(),
                new LightningBolt(), new GrizzlyBears()));

        castStream(player1.getId());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).hasSize(5);
    }

    @Test
    @DisplayName("Choosing fewer than the maximum leaves the rest in the graveyard")
    void choosingFewerLeavesRest() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castStream(player1.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));
        harness.passBothPriorities();

        // One remaining card + Stream of Consciousness itself
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 1);
    }

    @Test
    @DisplayName("Can target an opponent, shuffling their graveyard cards into their library")
    void canTargetOpponent() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LightningBolt()));
        int opponentLibBefore = gd.playerDecks.get(player2.getId()).size();

        castStream(player2.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibBefore + 2);
        harness.assertInGraveyard(player1, "Stream of Consciousness");
    }

    @Test
    @DisplayName("Casting with an empty target graveyard puts the spell on the stack directly")
    void emptyGraveyardPutsOnStack() {
        castStream(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stream of Consciousness");
    }
}
