package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HowlingGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking causes each player to draw a card")
    void attackingDrawsForEachPlayer() {
        addCreatureReady(player1, new HowlingGolem());

        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }

    @Test
    @DisplayName("Blocking causes each player to draw a card")
    void blockingDrawsForEachPlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HowlingGolem());

        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }

    @Test
    @DisplayName("Does nothing when it neither attacks nor blocks")
    void noDrawWhenNotInCombat() {
        addCreatureReady(player1, new HowlingGolem());

        declareAttackers(player1, List.of());

        assertThat(gd.stack).isEmpty();
    }
}
