package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spincrusher.class, AuriokGlaivemaster.class})
class SpincrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking puts a +1/+1 counter on Spincrusher")
    void blockingPutsCounterOnSpincrusher() {
        Permanent spincrusher = addCreatureReady(player2, new Spincrusher());
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
        Permanent spincrusher = addCreatureReady(player2, new Spincrusher());
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
        Permanent spincrusher = addCreatureReady(player2, new Spincrusher());
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

    @Test
    @DisplayName("An unblockable Spincrusher cannot be assigned a blocker")
    void cannotBeBlockedInCombat() {
        Permanent spincrusher = addCreatureReady(player1, new Spincrusher());
        spincrusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        spincrusher.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AuriokGlaivemaster());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Spincrusher cannot activate its ability without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent spincrusher = addCreatureReady(player1, new Spincrusher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(spincrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spincrusher.isCantBeBlocked()).isFalse();
    }

    private void addAttackingCreature(Player player) {
        Permanent attacker = addCreatureReady(player, new AuriokGlaivemaster());
        attacker.setAttacking(true);
    }

    private void declareBlock() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
