package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.o.OrcishCaptain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenSoldier.class, GrizzlyBears.class, OrcishCaptain.class, HighGround.class})
class DwarvenSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("When Dwarven Soldier becomes blocked by an Orc it gets +0/+2 until end of turn")
    void becomesBlockedByOrcBoosts() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        addReadyCreature(player2, true); // Orc blocker

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Dwarven Soldier becomes blocked by a non-Orc it gets no boost")
    void becomesBlockedByNonOrcDoesNothing() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        addReadyCreature(player2, false); // non-Orc blocker

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("When Dwarven Soldier blocks an Orc it gets +0/+2 until end of turn")
    void blocksOrcBoosts() {
        addReadyCreature(player1, true); // Orc attacker
        Permanent soldier = addCreatureReady(player2, new DwarvenSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Dwarven Soldier blocks a non-Orc it gets no boost")
    void blocksNonOrcDoesNothing() {
        addReadyCreature(player1, false); // non-Orc attacker
        Permanent soldier = addCreatureReady(player2, new DwarvenSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("Blocked by an Orc and a non-Orc, Dwarven Soldier gets +0/+2 only once")
    void becomesBlockedByOrcAndNonOrcBoostsOnce() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        addReadyCreature(player2, true);  // Orc blocker
        addReadyCreature(player2, false); // non-Orc blocker

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Dwarven Soldier blocks multiple Orcs it gets +0/+2 only once")
    void blocksMultipleOrcsBoostsOnce() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent soldier = addCreatureReady(player2, new DwarvenSoldier());
        addReadyCreature(player1, true);
        addReadyCreature(player1, true);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocked by two Orcs, Dwarven Soldier gets +0/+2 only once")
    void becomesBlockedByTwoOrcsBoostsOnce() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        addReadyCreature(player2, true);
        addReadyCreature(player2, true);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        addReadyCreature(player2, true); // Orc blocker

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("A non-Orc that becomes an Orc after blocking was declared does not trigger the ability")
    void becomesBlockedTriggerUsesSubtypeAtBlockTime() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        Permanent blocker = addReadyCreature(player2, false);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        PermanentGrants.subtype(gd, blocker, CardSubtype.ORC);
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("An attacker that becomes an Orc after blocking was declared does not trigger the ability")
    void blockingTriggerUsesSubtypeAtBlockTime() {
        Permanent attacker = addReadyCreature(player1, false);
        Permanent soldier = addCreatureReady(player2, new DwarvenSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        PermanentGrants.subtype(gd, attacker, CardSubtype.ORC);
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("An Orc that loses its subtype after becoming blocked still triggers the ability")
    void becomesBlockedTriggerRemembersOrcAtBlockTime() {
        Permanent soldier = addCreatureReady(player1, new DwarvenSoldier());
        Permanent blocker = addMutableOrcCreature(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.endFloatingEffect(orcGrants.get(blocker));
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("An Orc that loses its subtype after blocking still triggers the ability")
    void blockingTriggerRemembersOrcAtBlockTime() {
        Permanent attacker = addMutableOrcCreature(player1);
        Permanent soldier = addCreatureReady(player2, new DwarvenSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.endFloatingEffect(orcGrants.get(attacker));
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    private Permanent addReadyCreature(Player player, boolean orc) {
        return addCreatureReady(player, orc ? new OrcishCaptain() : new GrizzlyBears());
    }

    private final Map<Permanent, FloatingContinuousEffect> orcGrants = new HashMap<>();

    private Permanent addMutableOrcCreature(Player player) {
        Permanent permanent = addCreatureReady(player, new GrizzlyBears());
        orcGrants.put(permanent, PermanentGrants.subtype(gd, permanent, CardSubtype.ORC));
        return permanent;
    }
}
