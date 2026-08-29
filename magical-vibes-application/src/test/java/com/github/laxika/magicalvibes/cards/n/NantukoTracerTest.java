package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoTracer.class, GrizzlyBears.class})
class NantukoTracerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a targeted card from any graveyard on the bottom of its owner's library")
    void etbPutsTargetedCardOnOwnersLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingLibraryCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setLibrary(player2, new ArrayList<>(List.of(existingLibraryCard)));

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
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.setLibrary(player1, new ArrayList<>());

        castTracer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castTracer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new NantukoTracer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
