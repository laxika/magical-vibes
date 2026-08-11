package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TangleTest extends BaseCardTest {

    @Test
    void preventsCombatDamageAndKeepsAttackingCreaturesTapped() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap();

        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        nonAttacker.setSummoningSick(false);
        nonAttacker.tap();

        castAndResolve();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(nonAttacker.getSkipUntapCount()).isZero();

        advanceToNextTurn(player1);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(nonAttacker.isTapped()).isFalse();
    }

    @Test
    void attackingCreaturesUntapOnTheFollowingTurn() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap();

        castAndResolve();
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(attacker.isTapped()).isFalse();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new Tangle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
