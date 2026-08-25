package com.github.laxika.magicalvibes.cards.u;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({UnexplainedVision.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class})
class UnexplainedVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards without adamant")
    void drawsThreeCards() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Island island = new Island();
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, bears, island, mountain));
        castUnexplainedVision(1, 4);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, bears, island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Adamant scries three after drawing three cards")
    void adamantScriesAfterDrawingThreeCards() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Island island = new Island();
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, bears, island, mountain));
        castUnexplainedVision(3, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, bears, island);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(mountain);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
    }

    private void castUnexplainedVision(int blueMana, int colorlessMana) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new UnexplainedVision()));
        harness.addMana(player1, ManaColor.BLUE, blueMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
