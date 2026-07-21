package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarisisTwinclawsTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike — attacking a player deals combat damage twice (2 + 2 = 4)")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = new Permanent(new MarisisTwinclaws());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Marisi's Twinclaws (2/4) with intrinsic double strike deals 2 + 2 = 4 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
