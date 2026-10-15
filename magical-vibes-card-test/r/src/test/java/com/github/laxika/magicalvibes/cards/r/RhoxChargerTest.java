package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxCharger.class, GrizzlyBears.class})
class RhoxChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted Ă˘â‚¬â€ť the Charger attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent charger = addCreatureReady(player1, new RhoxCharger());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted Ă˘â‚¬â€ť another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new RhoxCharger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        Permanent charger = addCreatureReady(player1, new RhoxCharger());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new RhoxCharger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack Ă˘â‚¬â€ť not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Rhox Charger"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Rhox Charger contributes an exalted trigger to a lone attacker")
    void multipleExaltedAbilitiesStack() {
        Permanent attacker = addCreatureReady(player1, new RhoxCharger());
        addCreatureReady(player1, new RhoxCharger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exalted resolves even after its source leaves the battlefield")
    void exaltedSurvivesSourceRemoval() {
        Permanent source = addCreatureReady(player1, new RhoxCharger());
        Permanent attacker = addCreatureReady(player1, new RhoxCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's Rhox Charger does not exalt your attacker")
    void opponentExaltedDoesNotApply() {
        addCreatureReady(player1, new RhoxCharger());
        Permanent attacker = addCreatureReady(player2, new RhoxCharger());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted Rhox Charger tramples over a blocker")
    void exaltedPowerContributesToTrampleDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RhoxCharger());
        Permanent blocker = addCreatureReady(player2, new RhoxCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Rhox Charger");
        harness.assertOnBattlefield(player1, "Rhox Charger");
    }
}
