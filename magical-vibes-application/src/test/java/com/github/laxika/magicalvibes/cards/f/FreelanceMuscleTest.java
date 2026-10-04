package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfDenial;
import com.github.laxika.magicalvibes.cards.y.Yare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreelanceMuscle.class, CrawWurm.class, WallOfDenial.class, GrizzlyBears.class, Yare.class})
class FreelanceMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X on attack where X is the greatest other power or toughness")
    void boostsOnAttackByGreatestOtherPowerOrToughness() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        addCreatureReady(player1, new CrawWurm());
        addCreatureReady(player1, new WallOfDenial());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(12);
    }

    @Test
    @DisplayName("Gets the same boost when it blocks")
    void boostsOnBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent muscle = addCreatureReady(player2, new FreelanceMuscle());
        addCreatureReady(player2, new WallOfDenial());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(12);
    }

    @Test
    @DisplayName("Does not count itself among other creatures")
    void doesNotCountItself() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(4);
    }

    @Test
    @DisplayName("Uses power when it exceeds every other creature's toughness")
    void usesGreatestPower() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(10);
    }

    @Test
    @DisplayName("Ignores creatures controlled by the opponent")
    void ignoresOpposingCreatures() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        addCreatureReady(player2, new FreelanceMuscle());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts a different Freelance Muscle and keeps the resolved boost after it leaves")
    void countsAnotherMuscleAndLocksInBoost() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        Permanent other = addCreatureReady(player1, new FreelanceMuscle());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(8);
    }

    @Test
    @DisplayName("Counts creatures that enter after the attack trigger is put on the stack")
    void evaluatesOtherCreaturesAtResolution() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        addCreatureReady(player1, new FreelanceMuscle());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not count another creature that leaves before the trigger resolves")
    void ignoresCreaturesThatLeaveBeforeResolution() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        Permanent other = addCreatureReady(player1, new FreelanceMuscle());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(other);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(4);
    }

    @Test
    @DisplayName("Triggers only once when blocking multiple attackers")
    void triggersOnceWhenBlockingMultipleCreatures() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        firstAttacker.setAttackTarget(player2.getId());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondAttacker.setAttacking(true);
        secondAttacker.setAttackTarget(player2.getId());
        Permanent muscle = addCreatureReady(player2, new FreelanceMuscle());
        addCreatureReady(player2, new FreelanceMuscle());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0, muscle.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(8);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        addCreatureReady(player1, new WallOfDenial());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(12);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(4);
    }
}
