package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LockjawSnapperTest extends BaseCardTest {

    /**
     * Sets up combat where Lockjaw Snapper (player1) attacks and is blocked by a 3/3 creature (player2),
     * so the Snapper dies from combat damage.
     */
    private void setupCombatWhereSnapperDies() {
        Permanent snapperPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lockjaw Snapper"))
                .findFirst().orElseThrow();
        snapperPerm.setSummoningSick(false);
        snapperPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Death trigger adds a -1/-1 counter to each creature that already has one; others unaffected")
    void deathTriggerAddsCounterToWoundedCreatures() {
        harness.addToBattlefield(player1, new LockjawSnapper());

        GrizzlyBears woundedBear = new GrizzlyBears();
        woundedBear.setPower(4);
        woundedBear.setToughness(4);
        harness.addToBattlefield(player2, woundedBear);
        UUID woundedId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent wounded = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(woundedId)).findFirst().orElseThrow();
        wounded.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        GrizzlyBears healthyBear = new GrizzlyBears();
        harness.addToBattlefield(player2, healthyBear);
        UUID healthyId = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && !p.getId().equals(woundedId))
                .findFirst().orElseThrow().getId();

        setupCombatWhereSnapperDies();

        harness.passBothPriorities(); // Combat damage — Snapper dies, trigger goes on stack
        harness.passBothPriorities(); // Resolve the death trigger

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lockjaw Snapper"));
        assertThat(wounded.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        Permanent healthy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(healthyId)).findFirst().orElseThrow();
        assertThat(healthy.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Death trigger reduces a wounded 1/1 to 0/0 and it dies via SBA")
    void deathTriggerKillsWoundedCreature() {
        harness.addToBattlefield(player1, new LockjawSnapper());

        GrizzlyBears woundedBear = new GrizzlyBears(); // 2/2
        harness.addToBattlefield(player2, woundedBear);
        UUID woundedId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(woundedId)).findFirst().orElseThrow()
                .setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1); // now a 1/1

        setupCombatWhereSnapperDies();

        harness.passBothPriorities(); // Combat damage — Snapper dies, trigger on stack
        harness.passBothPriorities(); // Resolve the death trigger — wounded bear -> 0/0

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(woundedId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
