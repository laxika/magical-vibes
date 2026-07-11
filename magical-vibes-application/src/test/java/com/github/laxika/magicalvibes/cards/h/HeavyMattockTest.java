package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeavyMattockTest extends BaseCardTest {

    // ===== Card properties =====

    

    @Test
    @DisplayName("Heavy Mattock has equip {2} ability at sorcery speed")
    void hasEquipAbility() {
        HeavyMattock card = new HeavyMattock();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects())
                .anyMatch(e -> e instanceof EquipEffect);
    }

    // ===== Static boost: non-Human =====

    @Test
    @DisplayName("Equipped non-Human creature gets +1/+1")
    void equippedNonHumanGetsBaseBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(creature.getId());

        // Grizzly Bears 2/2 -> 3/3
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    // ===== Static boost: Human =====

    @Test
    @DisplayName("Equipped Human creature gets an additional +1/+1 for +2/+2 total")
    void equippedHumanGetsAdditionalBoost() {
        Permanent human = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());

        // Elite Vanguard 2/1 -> 4/3
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    @Test
    @DisplayName("Moving Heavy Mattock from a Human to a non-Human removes the additional boost")
    void movingFromHumanToNonHumanRemovesAdditionalBoost() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent bear = addReadyCreature(player1);

        mattock.setAttachedTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);

        // Re-equip to the non-Human
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(mattock.getAttachedTo()).isEqualTo(bear.getId());
        // Human reverts to base, bear only gets +1/+1
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    // ===== Removal / isolation =====

    @Test
    @DisplayName("Equipped creature loses boost when Heavy Mattock is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent human = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mattock);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Heavy Mattock does not affect creatures it is not attached to")
    void doesNotAffectOtherCreatures() {
        Permanent human = addReadyHuman(player1);
        Permanent other = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    // ===== Equip ability resolution =====

    @Test
    @DisplayName("Activating equip targets the creature and resolving attaches the Mattock")
    void equipAttachesToTarget() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, human.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(human.getId());

        harness.passBothPriorities();

        assertThat(mattock.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addMattockReady(Player player) {
        Permanent perm = new Permanent(new HeavyMattock());
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

    private Permanent addReadyHuman(Player player) {
        Permanent perm = new Permanent(new EliteVanguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
