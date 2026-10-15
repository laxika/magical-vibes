package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
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

@CardUsed({CopperCarapace.class, LeoninSkyhunter.class})
class CopperCarapaceTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires three mana")
    void equipRequiresThreeMana() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(carapace.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Copper Carapace and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CopperCarapace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Copper Carapace")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Copper Carapace to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(carapace.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(carapace.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        carapace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Equipped creature loses boost when Copper Carapace is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        carapace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(carapace);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Copper Carapace does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent otherCreature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        carapace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature cannot be declared as a blocker")
    void equippedCreatureCannotBlock() {
        // Player1 attacks with a creature
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        attacker.setAttacking(true);

        // Player2 has a creature equipped with Copper Carapace
        Permanent blocker = addCreatureReady(player2, new LeoninSkyhunter());
        Permanent carapace = harness.addToBattlefieldAndReturn(player2, new CopperCarapace());
        carapace.setAttachedTo(blocker.getId());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Creature can block again after Copper Carapace is removed")
    void creatureCanBlockAfterEquipmentRemoved() {
        // Player1 attacks with a creature
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        attacker.setAttacking(true);

        // Player2 has a creature equipped with Copper Carapace
        Permanent blocker = addCreatureReady(player2, new LeoninSkyhunter());
        Permanent carapace = harness.addToBattlefieldAndReturn(player2, new CopperCarapace());
        carapace.setAttachedTo(blocker.getId());

        // Remove equipment from battlefield
        gd.playerBattlefields.get(player2.getId()).remove(carapace);

        prepareDeclareBlockers(player1);

        // Should not throw — creature can block again
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures can still block normally")
    void unequippedCreatureCanStillBlock() {
        // Player1 attacks with a creature
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        attacker.setAttacking(true);

        // Player2 has Copper Carapace on battlefield (unattached) and a creature
        harness.addToBattlefieldAndReturn(player2, new CopperCarapace());
        Permanent blocker = addCreatureReady(player2, new LeoninSkyhunter());

        prepareDeclareBlockers(player1);

        // Creature at index 1 (after the equipment at index 0)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Copper Carapace can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent creature1 = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent creature2 = addCreatureReady(player1, new LeoninSkyhunter());

        carapace.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(carapace.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent creature = addCreatureReady(player2, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(carapace.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreature() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, carapace.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(carapace.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(carapace.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A failed re-equip leaves the equipment on its previous creature")
    void targetLeavingDoesNotDetachEquipment() {
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent original = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent target = addCreatureReady(player1, new LeoninSkyhunter());
        carapace.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(carapace.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipment affects an attached creature controlled by an opponent")
    void equipmentAffectsOpponentsAttachedCreature() {
        Permanent attacker = addCreatureReady(player1, new LeoninSkyhunter());
        attacker.setAttacking(true);
        Permanent carapace = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        Permanent creature = addCreatureReady(player2, new LeoninSkyhunter());
        carapace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
