package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Octoprophet.class})
class OctoprophetTest extends BaseCardTest {

    @Test
    @DisplayName("Octoprophet scries 2 when it enters the battlefield")
    void etbScriesTwo() {
        harness.setHand(player1, List.of(new Octoprophet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Scry allows keeping, splitting, or bottoming both cards in the chosen order")
    void ordersLookedAtCards(int bottomCount) {
        Octoprophet first = new Octoprophet();
        Octoprophet second = new Octoprophet();
        Octoprophet third = new Octoprophet();
        Octoprophet opponentCard = new Octoprophet();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentCard));

        castOctoprophet();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        List<Integer> top = bottomCount == 0 ? List.of(1, 0)
                : bottomCount == 1 ? List.of(1) : List.of();
        List<Integer> bottom = bottomCount == 0 ? List.of()
                : bottomCount == 1 ? List.of(0) : List.of(1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(top, bottom));

        if (bottomCount == 0) {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        } else if (bottomCount == 1) {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        } else {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        }
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry with a one-card library looks at only that card")
    void scriesOneAvailableCard() {
        Octoprophet onlyCard = new Octoprophet();
        harness.setLibrary(player1, List.of(onlyCard));

        castOctoprophet();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry with an empty library completes without a choice")
    void emptyLibraryCompletesWithoutChoice() {
        harness.setLibrary(player1, List.of());

        castOctoprophet();

        harness.assertOnBattlefield(player1, "Octoprophet");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castOctoprophet() {
        harness.setHand(player1, List.of(new Octoprophet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
