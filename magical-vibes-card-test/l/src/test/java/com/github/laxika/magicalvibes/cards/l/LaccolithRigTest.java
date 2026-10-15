package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DefenderEnVec;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaccolithRig.class, DefenderEnVec.class, SealOfRemoval.class, FlaringPain.class})
class LaccolithRigTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger deals enchanted creature's power to a target creature and prevents its combat damage")
    void acceptingTriggerDealsDamageAndPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Accepting the trigger prevents the enchanted creature's combat damage")
    void acceptingTriggerPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Moving the Aura after the trigger does not change the enchanted creature")
    void movingAuraAfterTriggerKeepsOriginalEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        Permanent rig = addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        Permanent newEnchantedCreature = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);

        harness.handlePermanentChosen(player1, victim.getId());
        rig.setAttachedTo(newEnchantedCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(newEnchantedCreature.getId());
    }

    @Test
    @DisplayName("Declining the trigger deals no damage and does not prevent combat damage")
    void decliningTriggerDoesNothing() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Damage uses the enchanted creature's current power when the trigger resolves")
    void damageUsesCurrentPowerAtResolution() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);

        harness.handlePermanentChosen(player1, victim.getId());
        attacker.setPowerModifier(-1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Multiple blockers produce one trigger for the enchanted creature")
    void multipleBlockersProduceOneTrigger() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent firstBlocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent secondBlocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlocks(attacker, List.of(firstBlocker, secondBlocker));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("An unblocked enchanted creature does not trigger the Aura")
    void unblockedDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("The enchanted creature still deals damage after leaving the battlefield")
    void departedEnchantedCreatureDealsLastKnownPowerDamage() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new SealOfRemoval());
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, victim.getId());
        attacker.setPowerModifier(1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(seal),
                null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInHand(player1, "Defender en-Vec");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Assigning no combat damage still applies when damage cannot be prevented")
    void unpreventableDamageDoesNotOverrideNoCombatDamageAssignment() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Choosing to deal zero damage still stops combat damage assignment")
    void zeroPowerStillStopsCombatDamageAssignment() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, victim.getId());
        attacker.setPowerModifier(-2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("An illegal target stops the whole trigger, including the combat damage restriction")
    void departedTargetDoesNotStopCombatDamageAssignment() {
        Permanent attacker = addCreatureReady(player1, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new SealOfRemoval());
        Permanent blocker = addCreatureReady(player2, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(seal),
                null, victim.getId());
        harness.passBothPriorities();
        resolveCombat();

        harness.assertInHand(player2, "Defender en-Vec");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("The Aura controller chooses the target and optional damage for an opponent's creature")
    void auraControllerMakesChoicesForOpponentsCreature() {
        Permanent attacker = addCreatureReady(player2, new DefenderEnVec());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player1, new DefenderEnVec());
        Permanent victim = addCreatureReady(player2, new DefenderEnVec());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat(player2);

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addRigAttachedTo(Permanent creature) {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new LaccolithRig());
        rig.setAttachedTo(creature.getId());
        return rig;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareBlocks(attacker, List.of(blocker));
    }

    private void declareBlocks(Permanent attacker, List<Permanent> blockers) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
