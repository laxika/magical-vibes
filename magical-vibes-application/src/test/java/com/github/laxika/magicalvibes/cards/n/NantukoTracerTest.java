package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.k.KrosanReclamation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoTracer.class, KrosanReclamation.class})
class NantukoTracerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a targeted card from any graveyard on the bottom of its owner's library")
    void etbPutsTargetedCardOnOwnersLibraryBottom() {
        Card target = new KrosanReclamation();
        Card existingLibraryCard = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingLibraryCard));

        castTracer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(existingLibraryCard.getId(), target.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId());
    }

    @Test
    @DisplayName("The optional ETB may be declined")
    void etbMayBeDeclined() {
        Card target = new KrosanReclamation();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());

        castTracer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty graveyard produces no target choice")
    void emptyGraveyardProducesNoTargetChoice() {
        castTracer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void castTracer() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new NantukoTracer(), "{1}{G}");
        harness.passBothPriorities();
    }
}
