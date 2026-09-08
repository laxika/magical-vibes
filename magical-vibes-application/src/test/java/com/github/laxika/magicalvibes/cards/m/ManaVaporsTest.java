package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Lands target player controls do not untap during their next untap step")
    void landsDoNotUntapDuringNextUntapStep() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent bears = gd.playerBattlefields.get(player2.getId()).get(1);
        forest.tap();
        bears.tap();

        castAndResolve(player2.getId());
        advanceToNextTurn(player1);

        assertThat(forest.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Lands untap normally on the following turn")
    void landsUntapOnFollowingTurn() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        forest.tap();

        castAndResolve(player2.getId());
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not affect lands controlled by another player")
    void doesNotAffectOtherPlayersLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent casterForest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent targetForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        casterForest.tap();
        targetForest.tap();

        castAndResolve(player2.getId());
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(casterForest.isTapped()).isFalse();
        assertThat(targetForest.isTapped()).isTrue();
    }

    private void castAndResolve(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ManaVapors()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
