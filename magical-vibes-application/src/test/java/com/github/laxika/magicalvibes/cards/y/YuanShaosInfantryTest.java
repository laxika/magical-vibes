package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YuanShaosInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone makes Yuan Shao's Infantry unblockable this combat")
    void attacksAloneBecomesUnblockable() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(infantry.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Attacking alongside another creature leaves Yuan Shao's Infantry blockable")
    void attacksWithOthersStaysBlockable() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());
        addCreatureReady(player1, new ShuCavalry());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(infantry.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(infantry.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(infantry.isCantBeBlocked()).isFalse();
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
