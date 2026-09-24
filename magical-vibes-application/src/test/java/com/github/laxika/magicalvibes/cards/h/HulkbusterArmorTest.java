package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BraveBrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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
        Permanent armor = new Permanent(new HulkbusterArmor());
        armor.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(armor);
        return armor;
    }
}
