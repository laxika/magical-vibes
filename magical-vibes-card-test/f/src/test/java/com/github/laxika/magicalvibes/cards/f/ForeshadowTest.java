package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Foreshadow.class, Archangel.class})
class ForeshadowTest extends BaseCardTest {

    private void cast(com.github.laxika.magicalvibes.model.Player targetPlayer) {
        harness.setHand(player1, List.of(new Foreshadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayer.getId());
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.NameCardMillDrawChoice.class);
    }

    @Test
    @DisplayName("Milling the named card draws immediately and schedules next-upkeep draw")
    void matchDrawsAndSchedulesUpkeepDraw() {
        Card top = new Foreshadow();
        harness.setLibrary(player2, List.of(top));

        cast(player2);
        harness.handleListChoice(player1, "Foreshadow");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        // Immediate draw from the name match (Foreshadow already left the hand).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mismatch still mills and schedules upkeep draw but draws no immediate card")
    void mismatchMillsWithoutImmediateDraw() {
        Card top = new Archangel();
        harness.setLibrary(player2, List.of(top));

        cast(player2);
        harness.handleListChoice(player1, "Foreshadow");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Scheduled draw resolves at the next upkeep")
    void upkeepDrawResolves() {
        Card top = new Archangel();
        harness.setLibrary(player2, List.of(top));

        cast(player2);
        harness.handleListChoice(player1, "Foreshadow");

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Empty opponent library still schedules the upkeep draw")
    void emptyOpponentLibraryStillSchedulesUpkeepDraw() {
        harness.setLibrary(player2, List.of());

        cast(player2);
        harness.handleListChoice(player1, "Foreshadow");

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Foreshadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Interaction clears after resolving")
    void interactionClearsAfterResolve() {
        Card top = new Foreshadow();
        harness.setLibrary(player2, List.of(top));

        cast(player2);
        harness.handleListChoice(player1, "Foreshadow");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the top card is milled, even when the next card matches")
    void millsOnlyOneCard() {
        Card top = new Archangel();
        Card second = new Foreshadow();
        harness.setLibrary(player2, List.of(top, second));

        cast(player2);
        harness.handleListChoice(player1, "Foreshadow");

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can choose a real card name absent from the game")
    void canNameCardOutsideGame() {
        harness.setLibrary(player1, List.of(new Foreshadow()));
        harness.setLibrary(player2, List.of(new Foreshadow()));
        harness.setHand(player2, List.of());

        cast(player2);
        harness.handleListChoice(player1, "Archangel");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Foreshadow");
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Naming suggestions do not depend on hidden opponent cards")
    void namingSuggestionsDoNotRevealHiddenCards() {
        harness.setLibrary(player1, List.of(new Foreshadow()));
        harness.setLibrary(player2, List.of(new Foreshadow()));
        harness.setHand(player2, List.of());
        cast(player2);
        List<String> firstOptions = List.copyOf(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options());
        harness.handleListChoice(player1, "Foreshadow");

        harness.setLibrary(player1, List.of(new Foreshadow()));
        harness.setLibrary(player2, List.of(new Archangel()));
        cast(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyElementsOf(firstOptions);
    }
}
