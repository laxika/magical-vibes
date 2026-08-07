package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoragaInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature you control gets +3/+3 and must be blocked this turn")
    void boostsAndLuresAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castInvocation();

        for (Permanent bears : gd.playerBattlefields.get(player1.getId())) {
            assertThat(bears.getEffectivePower()).isEqualTo(5);
            assertThat(bears.getEffectiveToughness()).isEqualTo(5);
            assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent creatures are unaffected")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castInvocation();

        Permanent theirs = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A boosted creature must be blocked if the defender is able")
    void attackerMustBeBlocked() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castInvocation();

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Boost and must-be-blocked flag wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castInvocation();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    private void castInvocation() {
        harness.setHand(player1, List.of(new JoragaInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
