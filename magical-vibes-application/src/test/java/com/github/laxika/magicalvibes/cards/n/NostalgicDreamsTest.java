package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NostalgicDreams.class, GiantGrowth.class, GrizzlyBears.class})
class NostalgicDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards, returns X target graveyard cards, and exiles itself")
    void discardsReturnsAndExilesItself() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card firstCard = new GiantGrowth();
        Card secondCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(dreams, new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds())
                .contains(firstCard.getId(), secondCard.getId())
                .hasSize(4);

        harness.handleMultipleCardsChosen(player1, List.of(firstCard.getId(), secondCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard, secondCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Giant Growth", "Grizzly Bears");
    }

    @Test
    @DisplayName("X=0 returns no cards and still exiles itself")
    void xZeroReturnsNoCardsAndExilesItself() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card graveyardCard = new GiantGrowth();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(dreams));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
    }
}
