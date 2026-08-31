package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfDenial;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreelanceMuscle.class, CrawWurm.class, WallOfDenial.class, GrizzlyBears.class})
class FreelanceMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X on attack where X is the greatest other power or toughness")
    void boostsOnAttackByGreatestOtherPowerOrToughness() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        addCreatureReady(player1, new CrawWurm());
        addCreatureReady(player1, new WallOfDenial());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(10);
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

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(9);
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
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent muscle = addCreatureReady(player1, new FreelanceMuscle());
        addCreatureReady(player1, new WallOfDenial());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(9);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, muscle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, muscle)).isEqualTo(4);
    }
}
