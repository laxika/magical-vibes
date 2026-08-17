package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WallOfMist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinLocksmithTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes creatures with defender unable to block this turn")
    void creaturesWithDefenderCannotBlock() {
        addCreatureReady(player1, new GoblinLocksmith());
        Permanent wall = addCreatureReady(player2, new WallOfMist());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attacking does not stop creatures without defender from blocking")
    void creaturesWithoutDefenderCanBlock() {
        addCreatureReady(player1, new GoblinLocksmith());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(elves.isBlocking()).isTrue();
    }
}
