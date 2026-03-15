package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelAxeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Darksteel Axe has static +2/+0 boost effect")
    void hasStaticBoostEffect() {
        DarksteelAxe card = new DarksteelAxe();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Darksteel Axe has equip {2} ability with sorcery-speed restriction")
    void hasEquipAbility() {
        DarksteelAxe card = new DarksteelAxe();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).singleElement().isInstanceOf(EquipEffect.class);
    }

    // ===== Equip and boost =====

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent axe = addReadyAxe(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 base + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 base + 0
    }

    @Test
    @DisplayName("Resolving equip attaches Darksteel Axe to target creature")
    void resolvingEquipAttaches() {
        Permanent axe = addReadyAxe(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature loses boost when Darksteel Axe is moved to another creature")
    void creatureLosesBoostWhenReEquipped() {
        Permanent axe = addReadyAxe(player1);        // index 0
        Permanent creature1 = addReadyCreature(player1); // index 1
        Permanent creature2 = addReadyCreature(player1); // index 2
        axe.setAttachedTo(creature1.getId());

        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);

        // Re-equip to creature2
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost does not affect unequipped creatures")
    void doesNotAffectUnequippedCreatures() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent axe = addReadyAxe(player1);
        axe.setAttachedTo(creature1.getId());

        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadyAxe(Player player) {
        Permanent perm = new Permanent(new DarksteelAxe());
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
