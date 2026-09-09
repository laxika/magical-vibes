package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallTheGatewatch.class, ChandraNalaar.class, GrizzlyBears.class})
class CallTheGatewatchTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a planeswalker card and puts the chosen card into hand")
    void searchesForPlaneswalkerAndPutsItIntoHand() {
        Card planeswalker = new ChandraNalaar();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(planeswalker, creature));
        castCallTheGatewatch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(1)
                .allMatch(card -> card.hasType(CardType.PLANESWALKER));

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Chandra Nalaar");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer a non-planeswalker card")
    void doesNotOfferNonPlaneswalkerCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castCallTheGatewatch();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.hasType(CardType.CREATURE));
    }

    private void castCallTheGatewatch() {
        harness.setHand(player1, List.of(new CallTheGatewatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
    }
}
