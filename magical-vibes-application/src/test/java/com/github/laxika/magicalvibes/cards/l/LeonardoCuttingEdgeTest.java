package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardoCuttingEdge.class, GrizzlyBears.class, SoulWarden.class})
class LeonardoCuttingEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller gains life")
    void putsCounterOnLifeGain() {
        Permanent leonardo = harness.addToBattlefieldAndReturn(player1, new LeonardoCuttingEdge());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(leonardo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void opponentLifeGainDoesNotTrigger() {
        Permanent leonardo = harness.addToBattlefieldAndReturn(player1, new LeonardoCuttingEdge());
        harness.addToBattlefield(player2, new SoulWarden());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(leonardo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void separateLifeGainEventsEachPutACounterOnLeonardo() {
        Permanent leonardo = harness.addToBattlefieldAndReturn(player1, new LeonardoCuttingEdge());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(leonardo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void lifelinkGainingSeveralLifePutsOnlyOneCounterOnLeonardo() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoCuttingEdge());
        leonardo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        leonardo.setAttacking(true);
        leonardo.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(leonardo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void sneakReturnsAttackerAsCostAndEntersTappedAndAttacking() {
        Permanent attacker = addCreatureReady(player1, new LeonardoCuttingEdge());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoCuttingEdge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
            harness.assertInHand(player1, "Leonardo, Cutting Edge");
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            resolveAllTriggers();
        });

        Permanent leonardo = findPermanent(player1, "Leonardo, Cutting Edge");
        assertThat(leonardo.getId()).isNotEqualTo(attacker.getId());
        assertThat(leonardo.isTapped()).isTrue();
        assertThat(leonardo.isAttacking()).isTrue();
        assertThat(leonardo.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void sneakCannotBeCastDuringDeclareAttackers() {
        Permanent attacker = addCreatureReady(player1, new LeonardoCuttingEdge());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoCuttingEdge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void sneakRequiresAnAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new LeonardoCuttingEdge());
        harness.setHand(player1, List.of(new LeonardoCuttingEdge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void sneakCannotReturnABlockedAttackerEvenIfItsBlockerHasLeft() {
        Permanent attacker = addCreatureReady(player1, new LeonardoCuttingEdge());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.setBlockedThisCombat(true);
        harness.setHand(player1, List.of(new LeonardoCuttingEdge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }
}
