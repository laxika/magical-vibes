package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tangle.class, RazorfootGriffin.class})
class TangleTest extends BaseCardTest {

    @Test
    void preventsCombatDamageAndKeepsAttackingCreaturesTapped() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap();

        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        nonAttacker.setSummoningSick(false);
        nonAttacker.tap();

        castAndResolve();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(nonAttacker.getSkipUntapCount()).isZero();

        advanceToUpkeep(player2);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(nonAttacker.isTapped()).isFalse();
    }

    @Test
    void attackingCreaturesUntapOnTheFollowingTurn() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap();

        castAndResolve();
        advanceToUpkeep(player2);
        advanceToUpkeep(player1);
        advanceToUpkeep(player2);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void preventsCombatDamageFromAttackingCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new RazorfootGriffin());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        castAndResolve();
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new Tangle(), "{1}{G}");
        harness.passBothPriorities();
    }
}
