package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraspingScoundrel.class})
class GraspingScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 while attacking")
    void getsPowerBoostWhileAttacking() {
        Permanent scoundrel = addCreatureReady(player1, new GraspingScoundrel());

        assertThat(gqs.getEffectivePower(gd, scoundrel)).isEqualTo(1);

        scoundrel.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, scoundrel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the power boost when it stops attacking")
    void losesPowerBoostWhenNotAttacking() {
        Permanent scoundrel = addCreatureReady(player1, new GraspingScoundrel());
        scoundrel.setAttacking(true);

        scoundrel.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, scoundrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unblocked attacker deals two damage without boosting other copies")
    void dealsBoostedCombatDamageOnlyForAttackingCopy() {
        Permanent attacker = addCreatureReady(player1, new GraspingScoundrel());
        Permanent nonattacker = addCreatureReady(player1, new GraspingScoundrel());
        Permanent defender = addCreatureReady(player2, new GraspingScoundrel());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, defender)).isEqualTo(1);

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A blocked attacker retains its bonus but a blocking copy gets no bonus")
    void blockedAttackerIsBoostedButBlockerIsNot() {
        Permanent attacker = addCreatureReady(player1, new GraspingScoundrel());
        Permanent blocker = addCreatureReady(player2, new GraspingScoundrel());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
    }
}
