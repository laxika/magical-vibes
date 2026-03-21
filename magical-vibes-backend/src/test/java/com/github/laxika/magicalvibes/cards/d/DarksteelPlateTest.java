package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelPlateTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Darksteel Plate has static grant indestructible to equipped creature effect")
    void hasGrantIndestructibleEffect() {
        DarksteelPlate card = new DarksteelPlate();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(grant.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(grant.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Darksteel Plate has equip {2} ability with sorcery-speed restriction")
    void hasEquipAbility() {
        DarksteelPlate card = new DarksteelPlate();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).singleElement().isInstanceOf(EquipEffect.class);
    }

    // ===== Equip =====

    @Test
    @DisplayName("Resolving equip attaches Darksteel Plate to target creature")
    void resolvingEquipAttaches() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Indestructible granted to equipped creature =====

    @Test
    @DisplayName("Equipped creature has indestructible keyword")
    void equippedCreatureHasIndestructible() {
        Permanent creature = addReadyCreature(player1);
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses indestructible when Darksteel Plate is removed")
    void creatureLosesIndestructibleWhenPlateRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(plate);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Unequipped creatures do not get indestructible")
    void unequippedCreatureDoesNotGetIndestructible() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature1.getId());

        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving Darksteel Plate transfers indestructible to new creature")
    void reEquipTransfersIndestructible() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        plate.setAttachedTo(creature1.getId());

        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyPlate(Player player) {
        Permanent perm = new Permanent(new DarksteelPlate());
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
