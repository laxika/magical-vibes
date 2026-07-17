package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BullCerodonTest extends BaseCardTest {

    @Test
    @DisplayName("Haste — attacks and deals damage the turn it enters while summoning sick")
    void hasteAllowsAttackWhileSummoningSick() {
        Permanent cerodon = new Permanent(new BullCerodon());
        gd.playerBattlefields.get(player1.getId()).add(cerodon);
        assertThat(cerodon.isSummoningSick()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Vigilance — attacking does not tap it")
    void vigilanceKeepsUntappedWhenAttacking() {
        Permanent cerodon = addCreatureReady(player1, new BullCerodon());

        declareAttackers(player1, List.of(0));

        assertThat(cerodon.isTapped()).isFalse();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
