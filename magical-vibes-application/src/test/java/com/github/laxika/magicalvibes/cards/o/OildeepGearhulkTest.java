package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

class OildeepGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets the controller choose any card from the target's hand")
    void promptsForAnyHandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        resolveOildeepGearhulkTargeting(player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.optional()).isTrue();
        assertThat(choice.validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Choosing a card makes the target discard it and draw a card")
    void discardsChosenCardAndDraws() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setLibrary(player2, List.of(new Island()));
        resolveOildeepGearhulkTargeting(player2.getId());

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Island");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the optional choice does not discard or draw")
    void decliningDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player2, List.of(new Island()));
        resolveOildeepGearhulkTargeting(player2.getId());

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    @Test
    @DisplayName("An empty hand produces no choice and no draw")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island()));
        resolveOildeepGearhulkTargeting(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    private void resolveOildeepGearhulkTargeting(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new OildeepGearhulk())));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
