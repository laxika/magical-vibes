package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorrosiveOoze.class, BalothGorger.class, ShortSword.class, LlanowarElves.class})
class CorrosiveOozeTest extends BaseCardTest {


    @Test
    @DisplayName("When Corrosive Ooze blocks an equipped creature, a trigger is created")
    void blockingEquippedCreatureCreatesTrigger() {
        Permanent ooze = addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();
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

        addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        // Equipment should be destroyed at end of combat
        harness.assertNotOnBattlefield(player1, "Short Sword");
        harness.assertInGraveyard(player1, "Short Sword");
    }

    @Test
    @DisplayName("When Corrosive Ooze blocks a non-equipped creature, no trigger is created")
    void blockingNonEquippedCreatureNoTrigger() {
        addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long oozeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Corrosive Ooze"))
                .count();
        assertThat(oozeTriggers).isZero();
    }


    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by an equipped creature, a trigger is created")
    void becomingBlockedByEquippedCreatureCreatesTrigger() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BalothGorger());
        Permanent equipment = addEquipment(player2);
        equipment.setAttachedTo(blocker.getId());

        prepareDeclareBlockers();
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

        Permanent blocker = addCreatureReady(player2, new BalothGorger());
        Permanent equipment = addEquipment(player2);
        equipment.setAttachedTo(blocker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        // Equipment should be destroyed at end of combat
        harness.assertNotOnBattlefield(player2, "Short Sword");
        harness.assertInGraveyard(player2, "Short Sword");
    }

    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by a non-equipped creature, no trigger is created")
    void becomingBlockedByNonEquippedCreatureNoTrigger() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        addCreatureReady(player2, new BalothGorger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long oozeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Corrosive Ooze"))
                .count();
        assertThat(oozeTriggers).isZero();
    }


    @Test
    @DisplayName("When Corrosive Ooze becomes blocked by equipped and non-equipped creatures, trigger only for equipped")
    void mixedBlockersTriggerOnlyForEquipped() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);

        Permanent equippedBlocker = addCreatureReady(player2, new BalothGorger());
        Permanent equipment = addEquipment(player2);
        equipment.setAttachedTo(equippedBlocker.getId());

        addCreatureReady(player2, new BalothGorger()); // non-equipped blocker

        prepareDeclareBlockers();
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


    @Test
    @DisplayName("All Equipment attached to the creature is destroyed, not just one")
    void multipleEquipmentAllDestroyed() {
        harness.setLife(player2, 20);

        addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);

        Permanent equipment1 = addEquipment(player1);
        equipment1.setAttachedTo(attacker.getId());
        Permanent equipment2 = addEquipment(player1);
        equipment2.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        // Both equipment should be destroyed
        long equipmentOnBattlefield = countPermanents(player1, "Short Sword");
        assertThat(equipmentOnBattlefield).isZero();
        long equipmentInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Short Sword"))
                .count();
        assertThat(equipmentInGraveyard).isEqualTo(2);
    }


    @Test
    void equipmentDestructionUsesTheStackAtEndOfCombat() {
        addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Short Sword");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Corrosive Ooze"));
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Short Sword");
    }

    @Test
    void equipmentOnCreatureThatDiesInCombatIsStillDestroyed() {
        addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Corrosive Ooze");
        harness.assertNotOnBattlefield(player1, "Short Sword");
        harness.assertInGraveyard(player1, "Short Sword");
    }

    @Test
    void eachEquippedBlockerCreatesItsOwnTrigger() {
        Permanent ooze = addReadyOoze(player1);
        ooze.setAttacking(true);
        Permanent first = addCreatureReady(player2, new BalothGorger());
        addEquipment(player2).setAttachedTo(first.getId());
        Permanent second = addCreatureReady(player2, new BalothGorger());
        addEquipment(player2).setAttachedTo(second.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(2, 0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry -> entry.isNonTargeting());
        assertThat(gd.stack).extracting(entry -> entry.getTargetId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    private Permanent addReadyOoze(Player player) {
        return addCreatureReady(player, new CorrosiveOoze());
    }

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ShortSword());
    }
}
