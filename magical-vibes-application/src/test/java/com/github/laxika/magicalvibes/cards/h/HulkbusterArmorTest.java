package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BraveBrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkbusterArmor.class, BraveBrawler.class, GrizzlyBears.class})
class HulkbusterArmorTest extends BaseCardTest {

    @Test
    void equippedCreatureBecomesNineNineAndGainsFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void bonusesEndWhenArmorIsDetached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        armor.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void equipHeroAbilityAttachesOnlyToHero() {
        Permanent armor = addArmorReady(player1);
        Permanent hero = addCreatureReady(player1, new BraveBrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, hero.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(hero.getId());
    }

    @Test
    void equipHeroAbilityRejectsNonHero() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Hero");

        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void genericEquipAbilityAttachesToNonHero() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addArmorReady(Player player) {
        Permanent armor = harness.addToBattlefieldAndReturn(player, new HulkbusterArmor());
        armor.setSummoningSick(false);
        return armor;
    }

    @Test
    void countersApplyOnTopOfNineNineBaseStats() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);
    }

    @Test
    void reequippingMovesBothBenefitsToNewCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        armor.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FLYING)).isTrue();
    }

    @Test
    void bothEquipAbilitiesRejectOpponentsHero() {
        Permanent armor = addArmorReady(player1);
        Permanent hero = addCreatureReady(player2, new BraveBrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, hero.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, hero.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void bothEquipAbilitiesRequireSorceryTiming() {
        Permanent armor = addArmorReady(player1);
        Permanent hero = addCreatureReady(player1, new BraveBrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, hero.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, hero.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void genericEquipCannotUseDiscountedHeroCost() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void disappearingEquipTargetLeavesOriginalAttachmentIntact() {
        Permanent armor = addArmorReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        armor.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
    }
}
