package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RumorGatherer.class, GrizzlyBears.class, CivilServant.class})
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
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Rumor Gatherer does not trigger for its own entry")
    void doesNotTriggerForOwnEntry() {
        CivilServant topCard = new CivilServant();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RumorGatherer(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Queued triggers choose scry or draw when they resolve")
    void queuedTriggersUseResolutionCount() {
        CivilServant firstCard = new CivilServant();
        CivilServant secondCard = new CivilServant();
        addRumorGatherer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.enterBattlefieldAndReturn(player1, new CivilServant());
        harness.enterBattlefieldAndReturn(player1, new CivilServant());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Rumor Gatherer counts its own ability resolutions")
    void separateGatherersHaveSeparateCounts() {
        CivilServant firstCard = new CivilServant();
        CivilServant secondCard = new CivilServant();
        CivilServant thirdCard = new CivilServant();
        addRumorGatherer();
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new RumorGatherer());
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));

        harness.enterBattlefieldAndReturn(player1, new CivilServant());
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                    .containsExactly(firstCard);
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new CivilServant());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolution count resets on the next player's turn")
    void resolutionCountResetsEachTurn() {
        CivilServant topCard = new CivilServant();
        CivilServant nextCard = new CivilServant();
        addRumorGatherer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.enterBattlefieldAndReturn(player1, new CivilServant());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new CivilServant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void addRumorGatherer() {
        harness.addToBattlefieldAndReturn(player1, new RumorGatherer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castCreatureAndResolveTrigger() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
