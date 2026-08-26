package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParkerLuck.class, GrizzlyBears.class, Shock.class, TrueBeliever.class, Forest.class})
class ParkerLuckTest extends BaseCardTest {

    @Test
    @DisplayName("Each target loses life based on the other target's revealed card")
    void losesLifeBasedOnOtherRevealedCard() {
        harness.addToBattlefield(player1, new ParkerLuck());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card player1Card = new GrizzlyBears();
        Card player2Card = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(player1Card);
        gd.playerDecks.get(player2.getId()).addFirst(player2Card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        chooseTargets();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Card);
    }

    @Test
    @DisplayName("A legal target still reveals and moves its card when the other library is empty")
    void emptyOtherLibraryStillUsesTheAvailableRevealedCard() {
        harness.addToBattlefield(player1, new ParkerLuck());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card player1Card = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(player1Card);
        gd.playerDecks.get(player2.getId()).clear();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        chooseTargets();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target does not prevent the legal target's card from moving")
    void resolvesForTheRemainingLegalTarget() {
        harness.addToBattlefield(player1, new ParkerLuck());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card player1Card = new GrizzlyBears();
        Card player2Card = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(player1Card);
        gd.playerDecks.get(player2.getId()).addFirst(player2Card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        chooseTargets();
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(player2Card);
        assertThat(gd.playerDecks.get(player2.getId())).contains(player2Card);
    }

    private void chooseTargets() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
    }
}
