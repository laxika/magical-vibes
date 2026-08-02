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

class SereneRemembranceTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles up to three targeted cards from the targeted graveyard into its owner's library")
    void shufflesTargetedCardsIntoLibrary() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(validIds).hasSize(2);
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 3); // 2 targets + Serene Remembrance
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library instead of going to the graveyard")
    void shufflesItselfIntoLibrary() {
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Serene Remembrance");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Serene Remembrance"));
    }

    @Test
    @DisplayName("Can target an opponent's graveyard")
    void canTargetOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LightningBolt()));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);
        // The spell itself goes to its own owner's library, not the targeted player's
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Serene Remembrance"));
    }

    @Test
    @DisplayName("Selection is capped at three cards")
    void selectionCappedAtThree() {
        Card extra = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt(), extra, new LightningBolt()));
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(4);
    }
}
