package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmmessiTome.class})
class EmmessiTomeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps Emmessi Tome and draws two cards, then prompts for a discard")
    void drawsTwoThenPromptsDiscard() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        harness.setHand(player1, List.of(new EmmessiTome()));
        harness.setLibrary(player1, List.of(new EmmessiTome(), new EmmessiTome()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tome.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Completing the discard leaves a net gain of one card")
    void discardLeavesNetGainOfOneCard() {
        harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        harness.setHand(player1, List.of(new EmmessiTome()));
        harness.setLibrary(player1, List.of(new EmmessiTome(), new EmmessiTome()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Emmessi Tome");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without five mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        harness.setHand(player1, List.of(new EmmessiTome()));
        harness.setLibrary(player1, List.of(new EmmessiTome(), new EmmessiTome()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        tome.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Drawing from an empty library ends the game before the discard")
    void emptyLibraryEndsGameBeforeDiscard() {
        harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws the available card and finishes discarding before empty-library loss")
    void drawsAvailableCardBeforeEmptyLibraryLoss() {
        harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        harness.setHand(player1, List.of());
        EmmessiTome lastCard = new EmmessiTome();
        harness.setLibrary(player1, List.of(lastCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lastCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
