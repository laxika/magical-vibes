package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutridCyclops.class, DryadArbor.class, Tarmogoyf.class})
class PutridCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying keeps the top card and gives Putrid Cyclops -X/-X by its mana value")
    void keepsTopCardAndGetsMinusByItsManaValue() {
        Card cyclopsCard = new PutridCyclops();
        Card topCard = new Tarmogoyf();
        harness.setLibrary(player1, List.of(topCard, new DryadArbor()));
        harness.castFromHand(player1, cyclopsCard, "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent cyclops = findPermanent(player1, "Putrid Cyclops");
        assertThat(cyclops.getEffectivePower()).isEqualTo(1);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(1);
        assertThat(cyclops.getPowerModifier()).isEqualTo(-2);
        assertThat(cyclops.getToughnessModifier()).isEqualTo(-2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("The card left on top after scry determines the penalty")
    void bottomedCardIsNotUsedForPenalty() {
        Card cyclopsCard = new PutridCyclops();
        Card scriedCard = new DryadArbor();
        Card revealedCard = new Tarmogoyf();
        harness.setLibrary(player1, List.of(scriedCard, revealedCard));
        harness.castFromHand(player1, cyclopsCard, "{2}{B}");
        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        Permanent cyclops = findPermanent(player1, "Putrid Cyclops");
        assertThat(cyclops.getEffectivePower()).isEqualTo(1);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(revealedCard);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(scriedCard);
        assertThat(gameLogContains("reveals Tarmogoyf from the top of their library.")).isTrue();
    }

    @Test
    @DisplayName("The temporary penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        Card cyclopsCard = new PutridCyclops();
        harness.setLibrary(player1, List.of(new Tarmogoyf()));
        harness.castFromHand(player1, cyclopsCard, "{2}{B}");
        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent cyclops = findPermanent(player1, "Putrid Cyclops");
        assertThat(cyclops.getEffectivePower()).isEqualTo(1);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cyclops.getEffectivePower()).isEqualTo(3);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A land left on top gives no penalty")
    void landOnTopGivesNoPenalty() {
        Card cyclopsCard = new PutridCyclops();
        Card topCard = new DryadArbor();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, cyclopsCard, "{2}{B}");
        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent cyclops = findPermanent(player1, "Putrid Cyclops");
        assertThat(cyclops.getEffectivePower()).isEqualTo(3);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(3);
        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(cyclops.getToughnessModifier()).isZero();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("An empty library leaves Putrid Cyclops unchanged")
    void emptyLibraryGivesNoPenalty() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new PutridCyclops(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent cyclops = findPermanent(player1, "Putrid Cyclops");
        assertThat(cyclops.getEffectivePower()).isEqualTo(3);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealing a card with mana value three puts Putrid Cyclops into the graveyard")
    void lethalPenaltyPutsCyclopsIntoGraveyard() {
        Card topCard = new PutridCyclops();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new PutridCyclops(), "{2}{B}");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player1, "Putrid Cyclops");
        harness.assertInGraveyard(player1, "Putrid Cyclops");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

}
