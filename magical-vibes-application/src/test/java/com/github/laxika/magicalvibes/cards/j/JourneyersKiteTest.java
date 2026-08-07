package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JourneyersKiteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating offers only basic lands and keeps the Kite on the battlefield")
    void offersOnlyBasicLands() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Journeyer's Kite");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Exactly one chosen basic land goes to hand")
    void chosenLandGoesToHand() {
        activateSearch();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new JourneyersKite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Plains(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
    }
}
