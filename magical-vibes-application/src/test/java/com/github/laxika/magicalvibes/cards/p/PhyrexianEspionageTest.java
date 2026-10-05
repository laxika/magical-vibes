package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianEspionage.class, Forest.class, GrizzlyBears.class})
class PhyrexianEspionageTest extends BaseCardTest {

    @Test
    void drawsTwoCardsWithoutKicker() {
        Forest opponentForest = new Forest();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        Forest drawnForest = new Forest();
        GrizzlyBears drawnCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new PhyrexianEspionage()));
        harness.setHand(player2, List.of(opponentForest, opponentCreature));
        harness.setLibrary(player1, List.of(drawnForest, drawnCreature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnForest, drawnCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentForest, opponentCreature);
    }

    @Test
    void kickedDrawsTwoCardsAndEachOpponentDiscards() {
        Forest opponentForest = new Forest();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        Forest drawnForest = new Forest();
        GrizzlyBears drawnCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new PhyrexianEspionage()));
        harness.setHand(player2, List.of(opponentForest, opponentCreature));
        harness.setLibrary(player1, List.of(drawnForest, drawnCreature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnForest, drawnCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentForest);
    }

    @Test
    void kickedStillDrawsWhenOpponentHasNoCards() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        PhyrexianEspionage spell = new PhyrexianEspionage();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void drawsBeforeOpponentChoosesTheirDiscard() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        Forest firstOpponentCard = new Forest();
        Forest secondOpponentCard = new Forest();
        harness.setHand(player1, List.of(new PhyrexianEspionage()));
        harness.setHand(player2, List.of(firstOpponentCard, secondOpponentCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstOpponentCard, secondOpponentCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstOpponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondOpponentCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
