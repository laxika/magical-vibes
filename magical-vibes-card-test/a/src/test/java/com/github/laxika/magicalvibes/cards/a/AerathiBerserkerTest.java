package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerathiBerserker.class, BarbaryApes.class, Boomerang.class})
class AerathiBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Aerathi Berserker gets no rampage bonus")
    void oneBlockerGivesNoBonus() {
        Permanent berserker = addBerserkerAndDeclareBlockers(1);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("With two blockers Aerathi Berserker gets +3/+3 until end of turn")
    void twoBlockersGivePlusThreePlusThree() {
        Permanent berserker = addBerserkerAndDeclareBlockers(2);

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(7);
    }

    @Test
    @DisplayName("With three blockers Aerathi Berserker gets +6/+6 until end of turn")
    void threeBlockersGivePlusSixPlusSix() {
        Permanent berserker = addBerserkerAndDeclareBlockers(3);

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(6);
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(10);
    }

    @Test
    @DisplayName("Rampage bonus expires at end of turn")
    void rampageBonusExpiresAtEndOfTurn() {
        Permanent berserker = addBerserkerAndDeclareBlockers(2);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isZero();
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("If unblocked Aerathi Berserker gets no rampage bonus")
    void unblockedGivesNoBonus() {
        Permanent berserker = addCreatureReady(player1, new AerathiBerserker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isZero();
    }

    @Test
    @DisplayName("Rampage counts blockers when its trigger resolves")
    void blockerReturnedBeforeResolutionReducesBonus() {
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent berserker = addBerserkerAndDeclareBlockers(2, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing a blocker after rampage resolves does not change its bonus")
    void blockerReturnedAfterResolutionDoesNotReduceBonus() {
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent berserker = addBerserkerAndDeclareBlockers(2);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(5);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(7);
    }

    private Permanent addBerserkerAndDeclareBlockers(int blockerCount) {
        return addBerserkerAndDeclareBlockers(blockerCount, true);
    }

    private Permanent addBerserkerAndDeclareBlockers(int blockerCount, boolean resolveTrigger) {
        Permanent berserker = addCreatureReady(player1, new AerathiBerserker());
        for (int i = 0; i < blockerCount; i++) {
            addCreatureReady(player2, new BarbaryApes());
        }

        List<BlockerAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < blockerCount; i++) {
            assignments.add(new BlockerAssignment(i, 0));
        }
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, assignments);
        if (resolveTrigger) {
            harness.passBothPriorities();
        }
        return berserker;
    }
}
