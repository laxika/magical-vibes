package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.OutlawMedic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlanTheHeist.class, OutlawMedic.class})
class PlanTheHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils three before drawing three when the hand is empty")
    void surveilsThenDrawsWithEmptyHand() {
        Card topCard = new OutlawMedic();
        Card secondCard = new OutlawMedic();
        Card graveyardCard = new OutlawMedic();
        Card drawCard = new OutlawMedic();
        harness.setLibrary(player1, List.of(topCard, secondCard, graveyardCard, drawCard));
        harness.setHand(player1, List.of(new PlanTheHeist()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard, graveyardCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(0, 1), List.of(2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard, secondCard, drawCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    @DisplayName("Draws three without surveilling when the hand is not empty")
    void onlyDrawsWithNonEmptyHand() {
        Card firstCard = new OutlawMedic();
        Card secondCard = new OutlawMedic();
        Card thirdCard = new OutlawMedic();
        Card spareCard = new OutlawMedic();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new PlanTheHeist(), spareCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(
                spareCard, firstCard, secondCard, thirdCard);
    }

    @Test
    void canKeepAllSurveilledCardsInAnyOrder() {
        Card first = new OutlawMedic();
        Card second = new OutlawMedic();
        Card third = new OutlawMedic();
        Card fourth = new OutlawMedic();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new PlanTheHeist()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    void canPutAllSurveilledCardsIntoGraveyardBeforeDrawing() {
        Card first = new OutlawMedic();
        Card second = new OutlawMedic();
        Card third = new OutlawMedic();
        Card fourth = new OutlawMedic();
        Card fifth = new OutlawMedic();
        Card sixth = new OutlawMedic();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        harness.setHand(player1, List.of(new PlanTheHeist()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fourth, fifth, sixth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    @Test
    void plottingPaysFourManaAndAllowsFreeCastingOnlyAtSorcerySpeedOnLaterTurn() {
        PlanTheHeist spell = new PlanTheHeist();
        Card spare = new OutlawMedic();
        Card first = new OutlawMedic();
        Card second = new OutlawMedic();
        Card third = new OutlawMedic();
        Card fourth = new OutlawMedic();
        harness.setHand(player1, List.of(spell, spare));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setLibrary(player2, List.of(new OutlawMedic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spare);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(spare, first, second, third, fourth);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
