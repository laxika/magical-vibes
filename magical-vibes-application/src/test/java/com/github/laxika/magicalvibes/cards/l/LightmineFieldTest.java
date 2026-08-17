package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightmineFieldTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        return attacker;
    }

    private void declareAttacks(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, attackerIndices);
    }

    @Test
    @DisplayName("Triggers once and deals damage equal to the number of attackers to each attacker")
    void damagesEachAttackerByAttackerCount() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LightmineField()));
        Permanent attacker1 = addAttacker();
        Permanent attacker2 = addAttacker();

        declareAttacks(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker1.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker2.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not damage creatures that did not attack")
    void ignoresCreaturesThatDidNotAttack() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LightmineField()));
        Permanent attacker = addAttacker();
        Permanent stayedHome = addAttacker();

        declareAttacks(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(stayedHome.getMarkedDamage()).isZero();
    }
}
