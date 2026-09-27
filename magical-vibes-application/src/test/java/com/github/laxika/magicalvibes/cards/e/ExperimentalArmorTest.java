package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExperimentalArmor.class, GrizzlyBears.class})
class ExperimentalArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and has flying and haste")
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses are lost when Experimental Armor is unattached")
    void effectsAreLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        armor.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Experimental Armor to target creature")
    void equipAttachesToTargetCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addArmorReady(Player player) {
        Permanent permanent = new Permanent(new ExperimentalArmor());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
