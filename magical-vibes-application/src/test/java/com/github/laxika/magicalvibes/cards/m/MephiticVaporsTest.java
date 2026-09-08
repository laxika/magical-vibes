package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MephiticVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives every creature -1/-1 before surveilling 2")
    void weakensAllCreaturesBeforeSurveil() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        harness.setLibrary(player1, List.of(new AvatarOfMight(), new AvatarOfMight()));
        castMephiticVapors();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        harness.setLibrary(player1, List.of(new AvatarOfMight(), new AvatarOfMight()));
        castMephiticVapors();
        finishSurveil();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Surveil can put either of the top two cards into the graveyard")
    void surveilsTopTwoCards() {
        Card topCard = new AvatarOfMight();
        Card secondCard = new AvatarOfMight();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castMephiticVapors();

        finishSurveil(List.of(1), List.of(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    private void castMephiticVapors() {
        harness.setHand(player1, List.of(new MephiticVapors()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void finishSurveil() {
        finishSurveil(List.of(0, 1), List.of());
    }

    private void finishSurveil(List<Integer> topIndices, List<Integer> graveyardIndices) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(topIndices, graveyardIndices));
    }
}
