package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Curate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
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

@CardUsed({MatoyaArchonElder.class, Curate.class, Forest.class, Island.class, ZhalfirinVoid.class})
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
        harness.setHand(player1, List.of(new Curate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matoyaDrawnCard, curateDrawnCard);
    }
}
