package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianClaw.class, LeoninSkyhunter.class})
class ViridianClawTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Viridian Claw for {2} and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ViridianClaw()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Viridian Claw")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Claw to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(claw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);    // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature loses boost when Claw is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(claw);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Claw does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent otherCreature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has first strike")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses first strike when Claw is removed")
    void creatureLosesFirstStrikeWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(claw);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Claw can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature1 = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent creature2 = addCreatureReady(player1, new LeoninSkyhunter());

        claw.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(claw.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        // Equipment should still be on battlefield, unattached
        harness.assertOnBattlefield(player1, "Viridian Claw");
        assertThat(gd.stack).isEmpty();
        assertThat(claw.getAttachedTo()).isNull();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Equip requires one mana")
    void cannotEquipWithoutMana() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(claw.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, newly entered Claw can equip for one mana")
    void tappedEquipmentCanEquip() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        claw.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(claw.isAttached()).isFalse();
        harness.passBothPriorities();

        assertThat(claw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(claw.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = addCreatureReady(player2, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(claw.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature")
    void cannotEquipNoncreature() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, claw.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(claw.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(claw.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Failed re-equip preserves the previous attachment and bonuses")
    void failedReEquipPreservesOriginalAttachment() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent original = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent target = addCreatureReady(player1, new LeoninSkyhunter());
        claw.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(claw.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attached creature retains bonuses after its controller changes")
    void bonusesFollowCreatureAcrossControllers() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        claw.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.runStateBasedActions();

        assertThat(claw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Two Claws stack their boosts and removing one preserves first strike")
    void multipleClawsStackBonuses() {
        Permanent creature = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ViridianClaw());
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }
}
