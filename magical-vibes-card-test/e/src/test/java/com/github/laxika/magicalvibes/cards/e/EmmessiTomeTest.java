package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
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

@CardUsed({EmmessiTome.class, Disenchant.class})
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
    @DisplayName("An empty library and empty hand cause loss after resolution without a discard prompt")
    void emptyLibraryAndHandEndGameAfterResolution() {
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

    @Test
    @DisplayName("An empty library does not prevent discarding an existing hand card before losing")
    void discardsExistingCardBeforeEmptyLibraryLoss() {
        harness.addToBattlefield(player1, new EmmessiTome());
        EmmessiTome handCard = new EmmessiTome();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(handCard);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The activated ability draws and discards even if Tome is destroyed in response")
    void resolvesAfterSourceIsDestroyed() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new EmmessiTome());
        harness.setHand(player1, List.of());
        EmmessiTome firstDraw = new EmmessiTome();
        EmmessiTome secondDraw = new EmmessiTome();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tome.getId());
        harness.assertNotOnBattlefield(player1, "Emmessi Tome");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondDraw);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
