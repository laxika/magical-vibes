package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LaccolithRigTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger deals enchanted creature's power to a target creature and prevents its combat damage")
    void acceptingTriggerDealsDamageAndPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
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
    @DisplayName("Moving the Aura after the trigger does not change the enchanted creature")
    void movingAuraAfterTriggerKeepsOriginalEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent rig = addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        Permanent newEnchantedCreature = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addRigAttachedTo(attacker);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
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
    @DisplayName("An unblocked enchanted creature does not trigger the Aura")
    void unblockedDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addRigAttachedTo(attacker);
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    private Permanent addRigAttachedTo(Permanent creature) {
        Permanent rig = new Permanent(new LaccolithRig());
        rig.setSummoningSick(false);
        rig.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(rig);
        return rig;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();
    }
}
