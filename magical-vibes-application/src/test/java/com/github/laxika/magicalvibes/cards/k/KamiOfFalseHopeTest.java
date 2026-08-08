package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KamiOfFalseHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Kami of False Hope prevents all combat damage this turn")
    void sacrificePreventsCombatDamage() {
        harness.addToBattlefield(player1, new KamiOfFalseHope());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kami of False Hope");
        harness.assertInGraveyard(player1, "Kami of False Hope");
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage after the sacrifice")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KamiOfFalseHope());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
