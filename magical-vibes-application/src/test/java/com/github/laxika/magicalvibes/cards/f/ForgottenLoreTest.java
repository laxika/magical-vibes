package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForgottenLoreTest extends BaseCardTest {

    private void castForgottenLore(int extraGreenMana) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new ForgottenLore()));
        harness.addMana(player1, ManaColor.GREEN, 1 + extraGreenMana);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private PendingInteraction.GraveyardChoice activeGraveyardChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("Opponent chooses from the caster's graveyard; declining the {G} returns that card")
    void declineReturnsTheChosenCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Divination()));

        castForgottenLore(1);

        PendingInteraction.GraveyardChoice choice = activeGraveyardChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).extracting("name").containsExactly("Grizzly Bears", "Divination");

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        assertThat(gd.playerHands.get(player1.getId())).extracting("name").contains("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("name").containsExactly("Divination", "Forgotten Lore");
    }

    @Test
    @DisplayName("Paying {G} repeats the process and the already-chosen card can't be chosen again")
    void payingRepeatsAndExcludesChosenCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Divination()));

        castForgottenLore(2);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);

        PendingInteraction.GraveyardChoice second = activeGraveyardChoice();
        assertThat(second).isNotNull();
        assertThat(second.cardPool()).extracting("name").containsExactly("Divination");

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        // Only the last chosen card is returned.
        assertThat(gd.playerHands.get(player1.getId())).extracting("name").contains("Divination");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("name").containsExactly("Grizzly Bears", "Forgotten Lore");
    }

    @Test
    @DisplayName("No payment is offered when the caster can't afford {G}")
    void noPaymentPromptWithoutMana() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Divination()));

        castForgottenLore(0);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting("name").contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Paying with only one card left ends the loop and returns that card")
    void payingWithNoRemainingCardsEndsTheLoop() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castForgottenLore(1);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting("name").contains("Grizzly Bears");
    }

    @Test
    @DisplayName("An empty graveyard resolves with no choice and no card returned")
    void emptyGraveyardDoesNothing() {
        harness.setGraveyard(player1, List.of());

        castForgottenLore(1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent can't decline the graveyard choice")
    void graveyardChoiceIsMandatory() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castForgottenLore(1);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forgotten Lore can't target its own controller")
    void cannotTargetSelf() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForgottenLore()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
