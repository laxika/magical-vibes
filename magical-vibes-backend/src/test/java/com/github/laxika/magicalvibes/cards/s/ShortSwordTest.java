package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortSwordTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Short Sword has static +1/+1 boost effect")
    void hasStaticBoostEffect() {
        ShortSword card = new ShortSword();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Short Sword has equip ability with correct properties")
    void hasEquipAbility() {
        ShortSword card = new ShortSword();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Short Sword to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature loses boost when equipment is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Short Sword can be moved to another creature by equipping again")
    void canReEquipToAnotherCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        sword.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
    }

    // ===== Sorcery-speed timing restriction =====

    @Test
    @DisplayName("Cannot equip during opponent's turn")
    void cannotEquipDuringOpponentTurn() {
        addSwordReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot equip without enough mana")
    void cannotEquipWithoutMana() {
        addSwordReady(player1);
        Permanent creature = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helpers =====

    private Permanent addSwordReady(Player player) {
        Permanent perm = new Permanent(new ShortSword());
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
}
