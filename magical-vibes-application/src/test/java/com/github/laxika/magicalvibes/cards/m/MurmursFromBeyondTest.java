package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AkkiUnderling;
import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.c.CloudhoofKirin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurmursFromBeyond.class, ArabaMothrider.class, AkkiUnderling.class, CloudhoofKirin.class})
class MurmursFromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses one revealed card for the graveyard and the rest go to hand")
    void opponentChoosesGraveyardCard() {
        Card first = new ArabaMothrider();
        Card second = new AkkiUnderling();
        Card third = new CloudhoofKirin();
        Card untouched = new ArabaMothrider();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(second.getId())))
                .hasMessageContaining("Not your turn");

        harness.handleMultipleCardsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, third);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(second, untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A single revealed card is put into the graveyard without a choice")
    void singleRevealedCardGoesToGraveyard() {
        Card only = new CloudhoofKirin();
        harness.setLibrary(player1, List.of(only));
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With two cards available, the opponent chooses one and the other goes to hand")
    void opponentChoosesFromTwoRevealedCards() {
        Card first = new ArabaMothrider();
        Card second = new AkkiUnderling();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library leaves no cards to choose")
    void emptyLibraryDoesNotPrompt() {
        MurmursFromBeyond spell = new MurmursFromBeyond();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, spell, "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }
}
