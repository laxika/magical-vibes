package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ShroudedLore.class, GrizzlyBears.class, Divination.class})
class ShroudedLoreTest extends BaseCardTest {

    private void castShroudedLore(ShroudedLore spell, int extraBlackMana) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1 + extraBlackMana);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private PendingInteraction.GraveyardChoice activeGraveyardChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("Opponent chooses from the caster's graveyard; declining the {B} returns that card")
    void declineReturnsTheChosenCard() {
        Card chosenCard = new GrizzlyBears();
        Card otherCard = new Divination();
        ShroudedLore spell = new ShroudedLore();
        harness.setGraveyard(player1, List.of(chosenCard, otherCard));

        castShroudedLore(spell, 1);

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
    @DisplayName("Paying {B} repeats the process and excludes already-chosen cards")
    void payingRepeatsAndExcludesChosenCards() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Divination();
        ShroudedLore spell = new ShroudedLore();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));

        castShroudedLore(spell, 2);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.payOption("{B}"));

        PendingInteraction.GraveyardChoice second = activeGraveyardChoice();
        assertThat(second).isNotNull();
        assertThat(second.cardPool()).containsExactly(secondCard);

        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleListChoice(player1, ChoiceContext.ForgottenLorePaymentChoice.DECLINE);

        assertThat(gd.playerHands.get(player1.getId())).contains(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard, spell);
    }

    @Test
    @DisplayName("No payment is offered when the caster can't afford {B}")
    void noPaymentPromptWithoutMana() {
        Card chosenCard = new GrizzlyBears();
        Card otherCard = new Divination();
        ShroudedLore spell = new ShroudedLore();
        harness.setGraveyard(player1, List.of(chosenCard, otherCard));

        castShroudedLore(spell, 0);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
    }

    @Test
    @DisplayName("Shrouded Lore can't target its own controller")
    void cannotTargetSelf() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShroudedLore()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
