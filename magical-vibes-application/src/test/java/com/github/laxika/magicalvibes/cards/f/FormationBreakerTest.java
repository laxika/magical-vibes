package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormationBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        breaker.setAttacking(true);

        prepareFormationBreakerBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(breaker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        breaker.setAttacking(true);

        prepareFormationBreakerBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(breaker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+2 while you control a creature with any counter")
    void getsBoostWhileControllingCreatureWithCounter() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A counter on an opponent's creature does not provide the boost")
    void opponentCounterDoesNotProvideBoost() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(1);
    }

    private void prepareFormationBreakerBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
