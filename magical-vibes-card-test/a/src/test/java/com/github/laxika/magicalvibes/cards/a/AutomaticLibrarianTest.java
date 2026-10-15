package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AutomaticLibrarian.class})
class AutomaticLibrarianTest extends BaseCardTest {

    @Test
    @DisplayName("When Automatic Librarian enters, its controller scries 2")
    void scriesTwoOnEnter() {
        Card topCard = new AutomaticLibrarian();
        Card bottomCard = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.castFromHand(player1, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard, bottomCard);
    }

    @Test
    @DisplayName("Scrying 2 can reorder both cards and finish the ETB")
    void resolvesScryTwo() {
        Card topCard = new AutomaticLibrarian();
        Card bottomCard = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.castFromHand(player1, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Automatic Librarian");
    }

    @Test
    @DisplayName("Scry can keep both cards on top in either order without moving deeper cards")
    void keepsBothCardsOnTopInChosenOrder() {
        Card first = new AutomaticLibrarian();
        Card second = new AutomaticLibrarian();
        Card deeper = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(first, second, deeper));
        harness.castFromHand(player1, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, deeper);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry can put both cards below the rest of the library in either order")
    void putsBothCardsOnBottomInChosenOrder() {
        Card first = new AutomaticLibrarian();
        Card second = new AutomaticLibrarian();
        Card deeper = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(first, second, deeper));
        harness.castFromHand(player1, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deeper, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 looks at the only card when the library contains one card")
    void scriesWithOneCardLibrary() {
        Card onlyCard = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry with an empty library completes without a choice or a draw")
    void scriesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Automatic Librarian");
    }

    @Test
    @DisplayName("The second player's Librarian scries its controller's library only")
    void secondPlayerScriesOwnLibrary() {
        Card opponentCard = new AutomaticLibrarian();
        Card first = new AutomaticLibrarian();
        Card second = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(opponentCard));
        harness.setLibrary(player2, List.of(first, second));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AutomaticLibrarian(), "{3}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
