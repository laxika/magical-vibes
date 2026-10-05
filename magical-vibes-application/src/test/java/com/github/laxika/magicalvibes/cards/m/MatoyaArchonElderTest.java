package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Curate;
import com.github.laxika.magicalvibes.cards.d.DreamsOfLaguna;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MatoyaArchonElder.class, Curate.class, DreamsOfLaguna.class, Forest.class, Island.class, ZhalfirinVoid.class})
class MatoyaArchonElderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card whenever its controller scries")
    void drawsAfterScry() {
        addCreatureReady(player1, new MatoyaArchonElder());
        Card scriedCard = new Forest();
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(scriedCard, drawnCard));
        harness.setHand(player1, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Draws a card whenever its controller surveils")
    void drawsAfterSurveil() {
        addCreatureReady(player1, new MatoyaArchonElder());
        Card firstSurveilledCard = new Forest();
        Card secondSurveilledCard = new Forest();
        Card matoyaDrawnCard = new Island();
        Card curateDrawnCard = new Island();
        harness.setLibrary(player1,
                List.of(firstSurveilledCard, secondSurveilledCard, matoyaDrawnCard, curateDrawnCard));
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matoyaDrawnCard, curateDrawnCard);
    }

    @Test
    @DisplayName("Keeping the scried card on top still triggers the draw")
    void drawsKeptScryCard() {
        addCreatureReady(player1, new MatoyaArchonElder());
        Card keptCard = new Forest();
        harness.setLibrary(player1, List.of(keptCard, new Island()));
        harness.setHand(player1, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
    }

    @Test
    @DisplayName("The surveilling spell finishes drawing before Matoya's trigger resolves")
    void drawsAfterSurveillingSpellFinishes() {
        addCreatureReady(player1, new MatoyaArchonElder());
        Card spellDrawnCard = new Forest();
        Card triggerDrawnCard = new Island();
        harness.setLibrary(player1, List.of(spellDrawnCard, triggerDrawnCard, new Forest()));
        harness.castFromHand(player1, new DreamsOfLaguna(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spellDrawnCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spellDrawnCard, triggerDrawnCard);
    }

    @Test
    @DisplayName("An opponent's surveil does not trigger Matoya")
    void doesNotDrawForOpponentsSurveil() {
        addCreatureReady(player1, new MatoyaArchonElder());
        harness.setHand(player1, List.of());
        Card keptCard = new Forest();
        harness.setLibrary(player2, List.of(keptCard, new Island()));
        harness.castFromHand(player2, new DreamsOfLaguna(), "{1}{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's scry does not trigger Matoya")
    void doesNotDrawForOpponentsScry() {
        addCreatureReady(player1, new MatoyaArchonElder());
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Island()));
        harness.setHand(player2, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
