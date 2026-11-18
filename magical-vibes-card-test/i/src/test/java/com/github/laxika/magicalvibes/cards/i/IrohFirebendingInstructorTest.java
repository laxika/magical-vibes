package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

        assertThat(PermanentGrants.powerBoostOf(gd, iroh)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, iroh)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherAttacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherAttacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, nonAttacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonAttacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("Iroh's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new IrohFirebendingInstructor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("Iroh does not trigger when another creature attacks alone")
    void doesNotTriggerForAnotherCreatureAttackingAlone() {
        addCreatureReady(player1, new IrohFirebendingInstructor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("Iroh boosts itself when attacking alone")
    void boostsItselfWhenAttackingAlone() {
        Permanent iroh = addCreatureReady(player1, new IrohFirebendingInstructor());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, iroh)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, iroh)).isEqualTo(1);
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
            assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
    }
}
