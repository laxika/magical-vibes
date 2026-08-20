package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TresserhornSkyknightTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures with first strike")
    void preventsCombatDamageFromFirstStrikeCreatures() {
        Permanent skyknight = addCreatureReady(player2, new TresserhornSkyknight());
        skyknight.setBlocking(true);
        skyknight.addBlockingTarget(0);

        Permanent attacker = new Permanent(new WhiteKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tresserhorn Skyknight");
        assertThat(skyknight.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from creatures without first strike")
    void doesNotPreventCombatDamageFromOrdinaryCreatures() {
        Permanent skyknight = addCreatureReady(player2, new TresserhornSkyknight());
        skyknight.setBlocking(true);
        skyknight.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tresserhorn Skyknight");
        assertThat(skyknight.getMarkedDamage()).isEqualTo(2);
    }
}
