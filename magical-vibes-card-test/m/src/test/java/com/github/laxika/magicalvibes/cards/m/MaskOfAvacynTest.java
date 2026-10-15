package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskOfAvacyn.class, WalkingCorpse.class, MomentOfHeroism.class})
class MaskOfAvacynTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mask of Avacyn for {2} and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MaskOfAvacyn()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mask of Avacyn")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Mask to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);    // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Equipped creature loses boost when Mask is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mask);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mask does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent otherCreature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has hexproof")
    void equippedCreatureHasHexproof() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Creature loses hexproof when Mask is removed")
    void creatureLosesHexproofWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mask);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Mask can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        Permanent creature1 = addCreatureReady(player1, new WalkingCorpse());
        Permanent creature2 = addCreatureReady(player1, new WalkingCorpse());

        mask.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HEXPROOF)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HEXPROOF)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new MaskOfAvacyn());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target creature before resolution
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Walking Corpse"));

        harness.passBothPriorities();

        // Equipment should still be on battlefield, unattached
        assertThat(gd.stack).isEmpty();
        Permanent remaining = findPermanent(player1, "Mask of Avacyn");
        assertThat(remaining.getAttachedTo()).isNull();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Equip requires three mana")
    void equipRequiresThreeMana() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mask.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new MaskOfAvacyn());
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mask.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new MaskOfAvacyn());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the equipped creature")
    void opponentCannotTargetEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new MomentOfHeroism()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof permits the creature's controller to target it")
    void controllerCanTargetEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        mask.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("A failed re-equip leaves the Mask attached to its original creature")
    void failedReEquipKeepsOriginalAttachment() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        Permanent original = addCreatureReady(player1, new WalkingCorpse());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());
        mask.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the Mask in response to equip grants no bonuses")
    void removingMaskBeforeEquipResolvesGrantsNoBonuses() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfAvacyn());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mask);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

}
