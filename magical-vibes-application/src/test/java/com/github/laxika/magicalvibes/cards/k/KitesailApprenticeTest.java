package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KitesailApprenticeTest extends BaseCardTest {

    @Test
    void withoutEquipmentHasNoBonus() {
        Permanent apprentice = addApprentice(player1);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    void whileEquippedGetsBonusAndFlying() {
        Permanent apprentice = addApprentice(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(apprentice.getId());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    @Test
    void losesBonusWhenEquipmentIsDetached() {
        Permanent apprentice = addApprentice(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(apprentice.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    private Permanent addApprentice(Player player) {
        Permanent permanent = new Permanent(new KitesailApprentice());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addEquipment(Player player) {
        Permanent permanent = new Permanent(new SwiftfootBoots());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
