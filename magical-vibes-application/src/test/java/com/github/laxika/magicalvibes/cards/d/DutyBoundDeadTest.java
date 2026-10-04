package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DutyBoundDead.class, WalkingCorpse.class})
class DutyBoundDeadTest extends BaseCardTest {

    @Test
    void exaltedBoostsDutyBoundDeadWhenItAttacksAlone() {
        Permanent dead = addCreatureReady(player1, new DutyBoundDead());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, dead)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dead)).isEqualTo(3);
    }

    @Test
    void multipleExaltedAbilitiesBoostTheSameLoneAttacker() {
        addCreatureReady(player1, new DutyBoundDead());
        addCreatureReady(player1, new DutyBoundDead());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(4);
    }

    @Test
    void exaltedDoesNotBoostAnOpponentsLoneAttacker() {
        addCreatureReady(player1, new DutyBoundDead());
        Permanent corpse = addCreatureReady(player2, new WalkingCorpse());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent dead = addCreatureReady(player1, new DutyBoundDead());
        dead.tap();
        dead.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Duty-Bound Dead");
    }

    @Test
    void unusedRegenerationShieldExpiresAtEndOfTurn() {
        Permanent dead = addCreatureReady(player1, new DutyBoundDead());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(dead.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new DutyBoundDead());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new DutyBoundDead());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new DutyBoundDead());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        Permanent dead = addCreatureReady(player1, new DutyBoundDead());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Duty-Bound Dead from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent dead = addCreatureReady(player1, new DutyBoundDead());
        dead.setRegenerationShield(1);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new WalkingCorpse());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Duty-Bound Dead");
        assertThat(dead.isTapped()).isTrue();
        assertThat(dead.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Duty-Bound Dead dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent dead = addCreatureReady(player1, new DutyBoundDead());
        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new WalkingCorpse());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Duty-Bound Dead");
        harness.assertInGraveyard(player1, "Duty-Bound Dead");
    }
}
