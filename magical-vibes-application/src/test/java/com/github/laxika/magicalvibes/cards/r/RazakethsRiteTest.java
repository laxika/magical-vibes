package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RazakethsRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting searches library for any card and puts it into hand")
    void searchPutsCardIntoHand() {
        harness.setHand(player1, List.of(new RazakethsRite()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears()));

        harness.passBothPriorities(); // resolve → library search prompt

        // Unrestricted search: all library cards offered, cannot fail to find.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().canFailToFind()).isFalse();

        int bearsIndex = -1;
        for (int i = 0; i < search.params().cards().size(); i++) {
            if (search.params().cards().get(i).getName().equals("Grizzly Bears")) {
                bearsIndex = i;
                break;
            }
        }
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(bearsIndex));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Razaketh's Rite");
    }

    @Test
    @DisplayName("Cycling {B} discards Razaketh's Rite and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RazakethsRite()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Razaketh's Rite");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
