package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgottenLore.class, FyndhornElves.class, Forest.class})
class ForgottenLoreTest extends BaseCardTest {

    private ForgottenLore castForgottenLore(int extraGreenMana) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        ForgottenLore spell = new ForgottenLore();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1 + extraGreenMana);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        return spell;
    }

    private PendingInteraction.GraveyardChoice activeGraveyardChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("Opponent chooses from the caster's graveyard; declining the {G} returns that card")
    void declineReturnsTheChosenCard() {
        Card chosenCard = new FyndhornElves();
        Card otherCard = new Forest();
        harness.setGraveyard(player1, List.of(chosenCard, otherCard));

        ForgottenLore spell = castForgottenLore(1);

        PendingInteraction.GraveyardChoice choice = activeGraveyardChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(chosenCard, otherCard);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCard, spell);
    }

    @Test
    @DisplayName("Paying {G} repeats the process and the already-chosen card can't be chosen again")
    void payingRepeatsAndExcludesChosenCards() {
        Card firstCard = new FyndhornElves();
        Card secondCard = new Forest();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));

        ForgottenLore spell = castForgottenLore(2);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);

        PendingInteraction.GraveyardChoice second = activeGraveyardChoice();
        assertThat(second).isNotNull();
        assertThat(second.cardPool()).containsExactly(secondCard);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        // Only the last chosen card is returned.
        assertThat(gd.playerHands.get(player1.getId())).contains(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard, spell);
    }

    @Test
    @DisplayName("No payment is offered when the caster can't afford {G}")
    void noPaymentPromptWithoutMana() {
        Card chosenCard = new FyndhornElves();
        Card otherCard = new Forest();
        harness.setGraveyard(player1, List.of(chosenCard, otherCard));

        castForgottenLore(0);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
    }

    @Test
    @DisplayName("Paying with only one card left ends the loop and returns that card")
    void payingWithNoRemainingCardsEndsTheLoop() {
        Card onlyCard = new FyndhornElves();
        harness.setGraveyard(player1, List.of(onlyCard));

        castForgottenLore(1);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
    }

    @Test
    @DisplayName("Paying with no remaining cards still offers the next payment choice")
    void payingWithNoRemainingCardsOffersPaymentAgain() {
        Card onlyCard = new FyndhornElves();
        harness.setGraveyard(player1, List.of(onlyCard));

        castForgottenLore(2);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);

        PendingInteraction.ColorChoice payment = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(payment).isNotNull();
        assertThat(payment.playerId()).isEqualTo(player1.getId());
        assertThat(payment.options()).containsExactly(
                ChoiceContext.ForgottenLorePaymentChoice.PAY,
                ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
    }

    @Test
    @DisplayName("Paying twice repeats the process twice and returns only the last chosen card")
    void payingTwiceReturnsOnlyLastChosenCard() {
        Card firstCard = new FyndhornElves();
        Card secondCard = new Forest();
        Card thirdCard = new FyndhornElves();
        harness.setGraveyard(player1, List.of(firstCard, secondCard, thirdCard));

        ForgottenLore spell = castForgottenLore(2);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.PAY);

        PendingInteraction.GraveyardChoice last = activeGraveyardChoice();
        assertThat(last).isNotNull();
        assertThat(last.cardPool()).containsExactly(thirdCard);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard, secondCard, spell);
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
        harness.setGraveyard(player1, List.of(new FyndhornElves()));

        castForgottenLore(1);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forgotten Lore can't target its own controller")
    void cannotTargetSelf() {
        harness.setGraveyard(player1, List.of(new FyndhornElves()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForgottenLore()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
