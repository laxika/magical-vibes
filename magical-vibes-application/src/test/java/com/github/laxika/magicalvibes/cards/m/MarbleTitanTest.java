package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarbleTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creature with power 3+ does not untap while Marble Titan is out")
    void power3CreatureStaysTapped() {
        addReady(player1, new MarbleTitan());
        Permanent giant = addReady(player1, new HillGiant()); // 3/3
        giant.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creature with power under 3 untaps normally")
    void power2CreatureUntaps() {
        addReady(player1, new MarbleTitan());
        Permanent bears = addReady(player1, new GrizzlyBears()); // 2/2
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Marble Titan (a 3/3) locks itself as well")
    void marbleTitanLocksItself() {
        Permanent titan = addReady(player1, new MarbleTitan()); // 3/3
        titan.tap();

        advanceToNextTurn(player2);

        assertThat(titan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Affects opponents' creatures during their untap step")
    void affectsOpponentCreatures() {
        addReady(player1, new MarbleTitan());
        Permanent opponentGiant = addReady(player2, new HillGiant()); // 3/3
        opponentGiant.tap();

        // player2's untap step
        advanceToNextTurn(player1);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Marble Titan leaves, power 3+ creatures untap again")
    void untapsAfterTitanLeaves() {
        Permanent titan = addReady(player1, new MarbleTitan());
        Permanent giant = addReady(player1, new HillGiant()); // 3/3
        giant.tap();

        gd.playerBattlefields.get(player1.getId()).remove(titan);

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
