package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilsRetribution.class, GrizzlyBears.class, HillGiant.class})
class RoilsRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals five damage divided among attacking creatures")
    void dealsFiveDamageAmongAttackingCreatures() {
        Permanent first = addCreatureReady(player2, new HillGiant());
        Permanent second = addCreatureReady(player2, new HillGiant());
        first.setAttacking(true);
        second.setAttacking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player1, 0, Map.of(first.getId(), 2, second.getId(), 3));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void targetsBlockingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, Map.of(blocker.getId(), 5));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Rejects a creature that is neither attacking nor blocking")
    void rejectsIdleCreature() {
        Permanent idle = addCreatureReady(player2, new HillGiant());
        prepareSpell();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(idle.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assignments must sum to five")
    void assignmentsMustSumToFive() {
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(attacker.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can divide damage between attacking and blocking creatures")
    void dividesDamageBetweenAttackerAndBlocker() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, Map.of(attacker.getId(), 2, blocker.getId(), 3));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Each target must be assigned at least one damage")
    void rejectsZeroDamageAssignment() {
        Permanent first = addCreatureReady(player2, new HillGiant());
        Permanent second = addCreatureReady(player2, new HillGiant());
        first.setAttacking(true);
        second.setAttacking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(first.getId(), 5, second.getId(), 0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void rejectsPlayerTarget() {
        prepareSpell();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage assigned to an illegal target is not redistributed")
    void doesNotRedistributeDamageWhenTargetLeavesCombat() {
        Permanent first = addCreatureReady(player2, new HillGiant());
        Permanent second = addCreatureReady(player2, new HillGiant());
        first.setAttacking(true);
        second.setAttacking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player1, 0, Map.of(first.getId(), 2, second.getId(), 3));
        second.setAttacking(false);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
    }

    @Test
    @DisplayName("Deals no damage when every target leaves combat")
    void dealsNoDamageWhenAllTargetsLeaveCombat() {
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        prepareSpell();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player1, 0, Map.of(attacker.getId(), 5));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }


    private void prepareSpell() {
        harness.setHand(player1, List.of(new RoilsRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
