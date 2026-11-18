package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BullAurochs.class, BorealDruid.class})
class BullAurochsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with another Aurochs gives +1/+0")
    void boostsForEachOtherAttackingAurochs() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bullAurochs)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bullAurochs)).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+0 when attacking with two other Aurochs")
    void boostsForEachOtherAttackingAurochsTwice() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bullAurochs)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bullAurochs)).isZero();
    }

    @Test
    @DisplayName("Other attacking creatures that are not Aurochs do not count")
    void ignoresNonAurochs() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BorealDruid());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bullAurochs)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bullAurochs)).isZero();
    }

    @Test
    @DisplayName("Attacking alone does not count the source itself")
    void attackingAloneDoesNotBoostItself() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bullAurochs)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bullAurochs)).isZero();
    }

    @Test
    @DisplayName("Non-attacking Aurochs on either battlefield do not count or receive a boost")
    void ignoresNonAttackingAurochs() {
        Permanent attacker = addCreatureReady(player1, new BullAurochs());
        Permanent ally = addCreatureReady(player1, new BullAurochs());
        Permanent defender = addCreatureReady(player2, new BullAurochs());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, ally)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, defender)).isZero();
    }

    @Test
    @DisplayName("An attacking Aurochs that leaves before resolution is not counted")
    void countsOtherAurochsAtResolution() {
        Permanent attacker = addCreatureReady(player1, new BullAurochs());
        Permanent other = addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("Each attacker gets its own fixed boost until end of turn")
    void resolvedBoostRemainsFixedAndExpiresAtCleanup() {
        Permanent attacker = addCreatureReady(player1, new BullAurochs());
        Permanent other = addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other));
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }
}
