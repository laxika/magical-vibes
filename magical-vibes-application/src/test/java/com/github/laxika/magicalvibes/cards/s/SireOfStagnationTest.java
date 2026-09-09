package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SireOfStagnation.class, Forest.class, GrizzlyBears.class, Mountain.class})
class SireOfStagnationTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's land entering exiles their top two cards and makes the controller draw two")
    void opponentLandTriggersExileAndDraw() {
        Card firstExiled = new GrizzlyBears();
        Card secondExiled = new Mountain();
        Card remaining = new GrizzlyBears();
        Card firstDrawn = new GrizzlyBears();
        Card secondDrawn = new Mountain();

        harness.addToBattlefield(player1, new SireOfStagnation());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(firstExiled, secondExiled, remaining));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(firstExiled.getId(), secondExiled.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
    }

    @Test
    @DisplayName("The controller's own land entering does not trigger Sire of Stagnation")
    void ownLandDoesNotTrigger() {
        Card topCard = new GrizzlyBears();

        harness.addToBattlefield(player1, new SireOfStagnation());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
