package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PainSeerTest extends BaseCardTest {

    @Test
    @DisplayName("When Pain Seer becomes untapped, it puts the top card into hand and loses its mana value in life")
    void becomesUntappedRevealsTopCardAndLosesLife() {
        addTappedPainSeer(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting a land into hand after becoming untapped causes no life loss")
    void landCausesNoLifeLoss() {
        addTappedPainSeer(player1);
        Card topCard = new Forest();
        harness.setLibrary(player1, deckOf(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Becoming untapped does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        addTappedPainSeer(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addTappedPainSeer(Player player) {
        Permanent painSeer = harness.addToBattlefieldAndReturn(player, new PainSeer());
        painSeer.setSummoningSick(false);
        painSeer.tap();
        return painSeer;
    }

    private void resolveUntapTrigger(Player activePlayer) {
        Player opponent = activePlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
