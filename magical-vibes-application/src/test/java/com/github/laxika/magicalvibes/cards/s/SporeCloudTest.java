package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SporeCloudTest extends BaseCardTest {

    @Test
    void tapsBlockingCreaturesPreventsCombatDamageAndLocksCombatCreatures() {
        Permanent attacker = addCreature(player2);
        attacker.setAttacking(true);
        attacker.tap();

        Permanent blocker = addCreature(player2);
        blocker.setBlocking(true);

        Permanent uninvolved = addCreature(player2);

        castAndResolve();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(uninvolved.isTapped()).isFalse();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
        assertThat(uninvolved.getSkipUntapCount()).isZero();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    void affectedCreaturesSkipTheirNextUntapStepOnly() {
        Permanent attacker = addCreature(player2);
        attacker.setAttacking(true);
        attacker.tap();

        Permanent blocker = addCreature(player2);
        blocker.setBlocking(true);

        castAndResolve();
        advanceToNextTurn(player1);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(blocker.isTapped()).isFalse();
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SporeCloud()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
