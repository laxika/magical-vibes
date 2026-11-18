package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonPartisan.class, LeoninSkyhunter.class, GoForTheThroat.class})
class LoxodonPartisanTest extends BaseCardTest {

    @Test
    void battleCryBoostsOnlyOtherAttackersAfterResolving() {
        Permanent partisan = addCreatureReady(player1, new LoxodonPartisan());
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent nonattacker = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent opponent = addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(partisan.getId());
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, partisan)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
    }

    @Test
    void nonattackingPartisanDoesNotTrigger() {
        addCreatureReady(player1, new LoxodonPartisan());
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(1));
        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
    }

    @Test
    void attackingAloneTriggersWithoutBoostingItself() {
        Permanent partisan = addCreatureReady(player1, new LoxodonPartisan());
        addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, partisan)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, partisan)).isZero();
    }

    @Test
    void multiplePartisansBoostEachOtherAndBonusesAccumulate() {
        Permanent first = addCreatureReady(player1, new LoxodonPartisan());
        Permanent second = addCreatureReady(player1, new LoxodonPartisan());
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    void bonusExpiresAtCleanup() {
        addCreatureReady(player1, new LoxodonPartisan());
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    void triggerResolvesAfterSourceIsDestroyed() {
        Permanent partisan = addCreatureReady(player1, new LoxodonPartisan());
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(0, 1));
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, partisan.getId());
        harness.assertInGraveyard(player1, "Loxodon Partisan");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    void creatureRemovedFromCombatBeforeResolutionIsNotBoosted() {
        addCreatureReady(player1, new LoxodonPartisan());
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new LeoninSkyhunter());
        declareAttackers(List.of(0, 1));
        attacker.setAttacking(false);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }
}
