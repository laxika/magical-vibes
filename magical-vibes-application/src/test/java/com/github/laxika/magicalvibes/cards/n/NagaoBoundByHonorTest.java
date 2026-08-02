package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NagaoBoundByHonorTest extends BaseCardTest {

    @Test
    @DisplayName("Nagao boosts Samurai creatures you control when it attacks")
    void boostsSamuraiCreaturesOnAttack() {
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());
        Permanent nonSamurai = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonSamurai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSamurai)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido boosts Nagao when it blocks")
    void bushidoWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nagao = addCreatureReady(player2, new NagaoBoundByHonor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bushido boosts Nagao when it becomes blocked")
    void bushidoWhenBecomesBlocked() {
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        nagao.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(4);
    }
}
