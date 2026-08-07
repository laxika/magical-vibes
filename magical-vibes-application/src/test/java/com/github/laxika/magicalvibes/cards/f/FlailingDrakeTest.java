package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlailingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives the attacker +1/+1 until end of turn")
    void blockingBoostsAttacker() {
        addReadyDrake(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        Permanent boosted = findPermanent(player1, "Grizzly Bears");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked gives the blocker +1/+1 until end of turn")
    void becomingBlockedBoostsBlocker() {
        Permanent drake = addReadyDrake(player1);
        drake.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(drake.getId());

        harness.passBothPriorities();

        Permanent boosted = findPermanent(player2, "Air Elemental");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures boosts each blocker")
    void becomingBlockedByMultipleCreaturesBoostsEach() {
        Permanent drake = addReadyDrake(player1);
        drake.setAttacking(true);
        addCreatureReady(player2, new AirElemental());
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Flailing Drake"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> blockers = findPermanents(player2, "Air Elemental");
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(p -> p.getPowerModifier() == 1 && p.getToughnessModifier() == 1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent drake = addReadyDrake(player1);
        drake.setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Air Elemental");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    private Permanent addReadyDrake(Player player) {
        Permanent perm = new Permanent(new FlailingDrake());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
