package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FencingAceTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike — attacking a player deals combat damage twice (1 + 1 = 2)")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = new Permanent(new FencingAce());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Fencing Ace (1/1) with intrinsic double strike deals 1 + 1 = 2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
