package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfLostTruths.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class, AirElemental.class})
class SphinxOfLostTruthsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards, then discards three when not kicked")
    void drawsThenDiscardsWhenNotKicked() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new SphinxOfLostTruths(), new GrizzlyBears(), new AirElemental(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws three cards without discarding when kicked")
    void drawsOnlyWhenKicked() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new SphinxOfLostTruths(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unkicked Sphinx can discard the three cards it just drew from an empty hand")
    void discardsNewlyDrawnCardsWithNoOtherCardsInHand() {
        var drawnCards = List.of(new Forest(), new Island(), new Mountain());
        harness.setLibrary(player1, drawnCards);
        harness.castFromHand(player1, new SphinxOfLostTruths(), "{3}{U}{U}");

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast makes its controller draw and discard, even on an opponent's turn")
    void enteringWithoutBeingCastDrawsAndDiscardsForController() {
        var retainedCard = new Island();
        var opponentCard = new Forest();
        var drawnCards = List.of(new Forest(), new Island(), new Mountain());
        harness.setHand(player1, List.of(opponentCard));
        harness.setHand(player2, List.of(retainedCard));
        harness.setLibrary(player2, drawnCards);

        harness.enterBattlefieldAndReturn(player2, new SphinxOfLostTruths());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
