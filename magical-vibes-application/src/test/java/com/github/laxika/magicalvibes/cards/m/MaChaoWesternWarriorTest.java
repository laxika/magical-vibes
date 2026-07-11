package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaChaoWesternWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone makes Ma Chao unblockable this combat")
    void attacksAloneBecomesUnblockable() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Attacking alongside another creature does not make Ma Chao unblockable")
    void attacksWithOthersStaysBlockable() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());
        addCreatureReady(player1, new ShuCavalry());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maChao.isCantBeBlocked()).isFalse();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
