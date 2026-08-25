package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwellOnThePast.class, GrizzlyBears.class, LightningBolt.class})
class DwellOnThePastTest extends BaseCardTest {

    private void castDwell(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DwellOnThePast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Shuffles the chosen cards from the target player's graveyard into their library")
    void shufflesChosenCardsIntoLibrary() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LightningBolt()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castDwell(player2.getId());

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 2);
        harness.assertInGraveyard(player1, "Dwell on the Past");
    }

    @Test
    @DisplayName("Caps the selection at four cards")
    void capsSelectionAtFourCards() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new LightningBolt(), new GrizzlyBears(), new LightningBolt(), new GrizzlyBears()));

        castDwell(player1.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).hasSize(5);
    }

    @Test
    @DisplayName("Choosing zero cards leaves the graveyard unchanged")
    void choosingZeroCardsLeavesGraveyardUnchanged() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        castDwell(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
    }
}
