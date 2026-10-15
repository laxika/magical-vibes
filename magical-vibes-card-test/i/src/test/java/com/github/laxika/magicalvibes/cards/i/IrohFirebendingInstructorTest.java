package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrohFirebendingInstructor.class, GrizzlyBears.class, Unsummon.class})
class IrohFirebendingInstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Iroh gives all attacking creatures you control +1/+1")
    void boostsAttackingCreatures() {
        Permanent iroh = addCreatureReady(player1, new IrohFirebendingInstructor());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(iroh.getPowerModifier()).isEqualTo(1);
        assertThat(iroh.getToughnessModifier()).isEqualTo(1);
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(otherAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(nonAttacker.getPowerModifier()).isZero();
        assertThat(nonAttacker.getToughnessModifier()).isZero();
        assertThat(opponentCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Iroh's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new IrohFirebendingInstructor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Iroh does not trigger when another creature attacks alone")
    void doesNotTriggerForAnotherCreatureAttackingAlone() {
        addCreatureReady(player1, new IrohFirebendingInstructor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Iroh boosts itself when attacking alone")
    void boostsItselfWhenAttackingAlone() {
        Permanent iroh = addCreatureReady(player1, new IrohFirebendingInstructor());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        assertThat(iroh.getPowerModifier()).isEqualTo(1);
        assertThat(iroh.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Iroh's attack trigger boosts remaining attackers after Iroh leaves")
    void triggerResolvesAfterIrohLeaves() {
        Permanent iroh = addCreatureReady(player1, new IrohFirebendingInstructor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            harness.castInstant(player1, 0, iroh.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(iroh);
            assertThat(attacker.getPowerModifier()).isZero();
            resolveAllTriggers();
        });

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }
}
