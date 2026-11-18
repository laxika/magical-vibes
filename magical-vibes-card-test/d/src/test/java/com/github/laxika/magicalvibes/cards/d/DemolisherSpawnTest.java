package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemolisherSpawn.class, GrizzlyBears.class, Forest.class, Shock.class, Millstone.class})
class DemolisherSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, other attacking creatures get +4/+4")
    void deliriumBoostsOtherAttackingCreatures() {
        setUpDelirium();
        Permanent spawn = addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, spawn)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, nonAttacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonAttacker)).isZero();
    }

    @Test
    @DisplayName("Without delirium, the attack trigger does not boost creatures")
    void withoutDeliriumDoesNotBoostCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent spawn = addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, spawn)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        setUpDelirium();
        addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    private void setUpDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    @Test
    @DisplayName("Delirium must still be present when the attack trigger resolves")
    void losingDeliriumBeforeResolutionPreventsBoost() {
        setUpDelirium();
        addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("Gaining delirium after attacking does not create an attack trigger")
    void gainingDeliriumAfterAttackDoesNotBoost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        assertThat(gd.stack).isEmpty();
        setUpDelirium();
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("An enchantment creature contributes two types toward delirium")
    void deliriumCountsMultipleTypesOnOneCard() {
        harness.setGraveyard(player1, List.of(new DemolisherSpawn(), new Forest(), new Shock()));
        addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking Spawns boost one another and their boosts stack on other attackers")
    void multipleSpawnsBoostEachOther() {
        setUpDelirium();
        Permanent firstSpawn = addCreatureReady(player1, new DemolisherSpawn());
        Permanent secondSpawn = addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, firstSpawn)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstSpawn)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, secondSpawn)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondSpawn)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(8);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(8);
    }
}
