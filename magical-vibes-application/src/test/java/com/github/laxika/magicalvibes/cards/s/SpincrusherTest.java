package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpincrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking puts a +1/+1 counter on Spincrusher")
    void blockingPutsCounterOnSpincrusher() {
        Permanent spincrusher = addSpincrusherReady(player2);
        addAttackingCreature(player1);

        declareBlock();
        harness.passBothPriorities();

        assertThat(spincrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spincrusher.getEffectivePower()).isEqualTo(1);
        assertThat(spincrusher.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter makes Spincrusher unblockable this turn")
    void removingCounterMakesSpincrusherUnblockable() {
        Permanent spincrusher = addSpincrusherReady(player2);
        addAttackingCreature(player1);

        declareBlock();
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(spincrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spincrusher.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Spincrusher's unblockable effect expires at end of turn")
    void unblockableExpiresAtEndOfTurn() {
        Permanent spincrusher = addSpincrusherReady(player2);
        addAttackingCreature(player1);

        declareBlock();
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spincrusher.isCantBeBlocked()).isFalse();
    }

    private Permanent addSpincrusherReady(Player player) {
        Permanent permanent = new Permanent(new Spincrusher());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAttackingCreature(Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }

    private void declareBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
