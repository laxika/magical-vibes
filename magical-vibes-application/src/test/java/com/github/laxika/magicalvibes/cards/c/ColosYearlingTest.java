package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColosYearling.class, GrizzlyBears.class, Mountain.class})
class ColosYearlingTest extends BaseCardTest {

    @Test
    @DisplayName("{R} gives Colos Yearling +1/+0 until end of turn")
    void pumpAbilityBoostsPower() {
        Permanent yearling = addCreatureReady(player1, new ColosYearling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(1);
    }

    @Test
    @DisplayName("The pump ability can be activated repeatedly and the boosts stack")
    void pumpAbilityStacks() {
        Permanent yearling = addCreatureReady(player1, new ColosYearling());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mountainwalk prevents blocking while the defending player controls a Mountain")
    void mountainwalkPreventsBlockingWithDefendingMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent yearling = addAttackingYearling();

        assertThatThrownBy(() -> declareBlock(blocker, yearling))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountainwalk does not prevent blocking without a Mountain controlled by the defender")
    void mountainwalkAllowsBlockingWithoutDefendingMountain() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent yearling = addAttackingYearling();

        declareBlock(blocker, yearling);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk does not consider a Mountain controlled by the attacking player")
    void mountainwalkAllowsBlockingWithOnlyAttackingMountain() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent yearling = addAttackingYearling();

        declareBlock(blocker, yearling);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingYearling() {
        Permanent yearling = addCreatureReady(player1, new ColosYearling());
        yearling.setAttacking(true);
        return yearling;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    @Test
    @DisplayName("Colos Yearling's pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent yearling = addCreatureReady(player1, new ColosYearling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(1);
    }
}
