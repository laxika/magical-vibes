package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoraciousVermin;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TatteredRatter.class, GrizzlyBears.class, VoraciousVermin.class, AmoeboidChangeling.class})
class TatteredRatterTest extends BaseCardTest {

    @Test
    @DisplayName("A Rat that becomes blocked gets +2/+0 until end of turn")
    void blockedRatGetsBoost() {
        addCreatureReady(player1, new TatteredRatter());
        Permanent rat = addCreatureReady(player1, new VoraciousVermin());
        rat.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(rat.getPowerModifier()).isEqualTo(2);
        assertThat(rat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TatteredRatter());
        Permanent rat = addCreatureReady(player1, new VoraciousVermin());
        rat.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rat.getPowerModifier()).isZero();
        assertThat(rat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocked non-Rat does not get the boost")
    void blockedNonRatDoesNotGetBoost() {
        addCreatureReady(player1, new TatteredRatter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A Rat controlled by an opponent does not get this Ratter's boost")
    void opponentRatDoesNotGetThisRattersBoost() {
        addCreatureReady(player1, new TatteredRatter());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentRat = addCreatureReady(player2, new VoraciousVermin());
        opponentRat.setAttacking(true);

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(opponentRat.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple blockers cause only one boost for a Rat")
    void multipleBlockersCauseOneBoost() {
        Permanent ratter = addCreatureReady(player1, new TatteredRatter());
        Permanent rat = addCreatureReady(player1, new VoraciousVermin());
        rat.setAttacking(true);
        addCreatureReady(player2, new TatteredRatter());
        addCreatureReady(player2, new TatteredRatter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(rat.getPowerModifier()).isEqualTo(2);
        assertThat(rat.getToughnessModifier()).isZero();
        assertThat(ratter.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Each Ratter boosts each blocked Rat, but not an unblocked Rat")
    void multipleRattersBoostEachBlockedRat() {
        addCreatureReady(player1, new TatteredRatter());
        addCreatureReady(player1, new TatteredRatter());
        Permanent firstRat = addCreatureReady(player1, new VoraciousVermin());
        Permanent secondRat = addCreatureReady(player1, new VoraciousVermin());
        Permanent unblockedRat = addCreatureReady(player1, new VoraciousVermin());
        firstRat.setAttacking(true);
        secondRat.setAttacking(true);
        unblockedRat.setAttacking(true);
        addCreatureReady(player2, new TatteredRatter());
        addCreatureReady(player2, new TatteredRatter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 2), new BlockerAssignment(1, 3)));

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();
        assertThat(firstRat.getPowerModifier()).isEqualTo(4);
        assertThat(secondRat.getPowerModifier()).isEqualTo(4);
        assertThat(unblockedRat.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A creature that gained all creature types receives the Rat boost")
    void creatureThatBecameRatGetsBoost() {
        Permanent ratter = addCreatureReady(player1, new TatteredRatter());
        addCreatureReady(player1, new AmoeboidChangeling());
        addCreatureReady(player2, new TatteredRatter());

        harness.activateAbility(player1, 1, 0, null, ratter.getId());
        resolveAllTriggers();
        ratter.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(ratter.getPowerModifier()).isEqualTo(2);
        assertThat(ratter.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A Rat that lost all creature types does not trigger the boost")
    void creatureThatStoppedBeingRatDoesNotGetBoost() {
        addCreatureReady(player1, new TatteredRatter());
        Permanent rat = addCreatureReady(player1, new VoraciousVermin());
        addCreatureReady(player1, new AmoeboidChangeling());
        addCreatureReady(player2, new TatteredRatter());

        harness.activateAbility(player1, 2, 1, null, rat.getId());
        resolveAllTriggers();
        rat.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(rat.getPowerModifier()).isZero();
    }
}