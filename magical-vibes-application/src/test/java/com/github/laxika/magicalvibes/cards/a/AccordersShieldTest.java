package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AccordersShield.class, MoriokReaver.class})
class AccordersShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires three mana")
    void equipRequiresThreeMana() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Accorder's Shield for {0} and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Accorder's Shield")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Shield to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        shield.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(shield.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +0/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);    // 3 + 0
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5); // 2 + 3
    }

    @Test
    @DisplayName("Equipped creature loses boost when Shield is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shield does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        Permanent otherCreature = addCreatureReady(player1, new MoriokReaver());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses vigilance when Shield is removed")
    void creatureLosesVigilanceWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature with vigilance does not tap when attacking")
    void equippedCreatureDoesNotTapWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Shield can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature1 = addCreatureReady(player1, new MoriokReaver());
        Permanent creature2 = addCreatureReady(player1, new MoriokReaver());

        shield.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.VIGILANCE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.VIGILANCE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target creature before resolution
        gd.playerBattlefields.get(player1.getId())
                .remove(creature);

        harness.passBothPriorities();

        // Equipment should still be on battlefield, unattached
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shield);
        assertThat(shield.getAttachedTo()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player2, new MoriokReaver());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreature() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shield.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's main phase")
    void cannotEquipDuringOpponentsTurn() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated while a spell is on the stack")
    void cannotEquipWithNonemptyStack() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isAttached()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Failed re-equip leaves the original creature equipped")
    void failedReEquipPreservesAttachment() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent original = addCreatureReady(player1, new MoriokReaver());
        Permanent target = addCreatureReady(player1, new MoriokReaver());
        shield.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(shield.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, original, Keyword.VIGILANCE)).isTrue();
    }
}
