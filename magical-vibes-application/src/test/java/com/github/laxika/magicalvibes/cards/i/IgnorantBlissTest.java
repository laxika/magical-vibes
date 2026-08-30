package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgnorantBliss.class, Forest.class, GrizzlyBears.class})
class IgnorantBlissTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the hand face down, then returns and draws at the next end step")
    void exilesHandDrawsAndReturnsCardsAtNextEndStep() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new IgnorantBliss(), first, second)));
        harness.setLibrary(player1, List.of(drawn));
        castIgnorantBliss();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);

        Card replacement = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(replacement)));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(replacement, drawn, first, second);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Still draws a card when there are no other cards in hand")
    void drawsWithNoOtherCardsInHand() {
        Card drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new IgnorantBliss())));
        harness.setLibrary(player1, List.of(drawn));
        castIgnorantBliss();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void castIgnorantBliss() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
