package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarlordsAxe;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorrosiveOozeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect on ON_BLOCK and ON_BECOMES_BLOCKED")
    void hasCorrectEffects() {
        CorrosiveOoze card = new CorrosiveOoze();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).singleElement()
                .isInstanceOf(DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).singleElement()
                .isInstanceOf(DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect.class);
        assertThat(card.getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED).getFirst().triggerMode())
                .isEqualTo(TriggerMode.PER_BLOCKER);
    }

    // ===== Corrosive Ooze blocks an equipped creature =====

    @Test
    @DisplayName("When Corrosive Ooze blocks an equipped creature, a trigger is created")
    void blockingEquippedCreatureCreatesTrigger() {
        Permanent ooze = addReadyOoze(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Corrosive Ooze")
                        && se.getTargetId().equals(attacker.getId())
                        && se.getSourcePermanentId().equals(ooze.getId()));
    }

    @Test
    @DisplayName("When Corrosive Ooze blocks an equipped creature, equipment is destroyed at end of combat")
    void blockingEquippedCreatureDestroysEquipmentAtEndOfCombat() {
        harness.setLife(player2, 20);

        Permanent ooze = addReadyOoze(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the block trigger (marks equipment for destruction at end of combat)
        harness.passBothPriorities();
        // Pass through declare blockers priority
        harness.passBothPriorities();

        // Equipment should be destroyed at end of combat
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Warlord's Axe"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Warlord's Axe"));
    }

    @Test
    @DisplayName("When Corrosive Ooze blocks a non-equipped creature, no trigger is created")
    void blockingNonEquippedCreatureNoTrigger() {
        addReadyOoze(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long oozeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Corrosive Ooze"))
                .count();
        assertThat(oozeTriggers).isZero();
    }

    // ===== Corrosive Ooze becomes blocked by an equipped creature =====

    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by an equipped creature, a trigger is created")
    void becomingBlockedByEquippedCreatureCreatesTrigger() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        Permanent equipment = addEquipment(player2);
        equipment.setAttachedTo(blocker.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Corrosive Ooze")
                        && se.getTargetId().equals(blocker.getId())
                        && se.getSourcePermanentId().equals(ooze.getId()));
    }

    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by an equipped creature, equipment is destroyed at end of combat")
    void becomingBlockedByEquippedCreatureDestroysEquipmentAtEndOfCombat() {
        harness.setLife(player2, 20);

        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        Permanent equipment = addEquipment(player2);
        equipment.setAttachedTo(blocker.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the becomes-blocked trigger
        harness.passBothPriorities();
        // Pass through declare blockers priority
        harness.passBothPriorities();

        // Equipment should be destroyed at end of combat
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Warlord's Axe"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Warlord's Axe"));
    }

    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by a non-equipped creature, no trigger is created")
    void becomingBlockedByNonEquippedCreatureNoTrigger() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        addReadyCreature(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long oozeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Corrosive Ooze"))
                .count();
        assertThat(oozeTriggers).isZero();
    }

    // ===== Mixed blockers (equipped and non-equipped) =====

    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by equipped and non-equipped creatures, trigger only for equipped")
    void mixedBlockersTriggerOnlyForEquipped() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        Permanent equippedBlocker = addReadyCreature(player2);
        Permanent equipment = addEquipment(player2);
        equipment.setAttachedTo(equippedBlocker.getId());

        addReadyCreature(player2); // non-equipped blocker

        setupDeclareBlockers();
        // equippedBlocker is index 0, equipment is index 1, non-equipped creature is index 2
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(2, 0)
        ));

        long oozeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Corrosive Ooze"))
                .count();
        assertThat(oozeTriggers).isEqualTo(1);
        assertThat(gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Corrosive Ooze"))
                .findFirst().get().getTargetId()).isEqualTo(equippedBlocker.getId());
    }

    // ===== Multiple equipment =====

    @Test
    @DisplayName("All Equipment attached to the creature is destroyed, not just one")
    void multipleEquipmentAllDestroyed() {
        harness.setLife(player2, 20);

        Permanent ooze = addReadyOoze(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        Permanent equipment1 = addEquipment(player1);
        equipment1.setAttachedTo(attacker.getId());
        Permanent equipment2 = addEquipment(player1);
        equipment2.setAttachedTo(attacker.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the block trigger
        harness.passBothPriorities();
        // Pass through declare blockers priority
        harness.passBothPriorities();

        // Both equipment should be destroyed
        long equipmentOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Warlord's Axe"))
                .count();
        assertThat(equipmentOnBattlefield).isZero();
        long equipmentInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Warlord's Axe"))
                .count();
        assertThat(equipmentInGraveyard).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadyOoze(Player player) {
        Permanent perm = new Permanent(new CorrosiveOoze());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipment(Player player) {
        Permanent perm = new Permanent(new WarlordsAxe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
