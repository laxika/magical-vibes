package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OverbeingOfMythTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent overbeing = addOverbeingReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, overbeing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, overbeing)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent overbeing = addOverbeingReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, overbeing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, overbeing)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller's draw step draws an additional card")
    void drawsExtraOnDrawStep() {
        addOverbeingReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        advanceToDraw(player1); // turn-based draw: 1 card
        harness.passBothPriorities(); // resolve Overbeing trigger: +1 card

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Opponent's draw step does not draw an extra card for the controller")
    void doesNotDrawOnOpponentDrawStep() {
        addOverbeingReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    private Permanent addOverbeingReady(Player player) {
        OverbeingOfMyth card = new OverbeingOfMyth();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
