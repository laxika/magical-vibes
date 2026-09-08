package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RumorGatherer.class, GrizzlyBears.class})
class RumorGathererTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature entry triggers scry 1")
    void firstResolutionScries() {
        GrizzlyBears topCard = new GrizzlyBears();
        addRumorGatherer();
        harness.setLibrary(player1, List.of(topCard));

        castCreatureAndResolveTrigger();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("The second resolution draws instead of scrying, and the third scries again")
    void secondResolutionDrawsAndThirdResolutionScries() {
        GrizzlyBears firstCard = new GrizzlyBears();
        GrizzlyBears secondCard = new GrizzlyBears();
        addRumorGatherer();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        castCreatureAndResolveTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        castCreatureAndResolveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);

        castCreatureAndResolveTrigger();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
    }

    @Test
    @DisplayName("Does not trigger for a creature entering under an opponent's control")
    void doesNotTriggerForOpponentCreature() {
        addRumorGatherer();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addRumorGatherer() {
        harness.addToBattlefieldAndReturn(player1, new RumorGatherer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castCreatureAndResolveTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
