package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyhunterCubTest extends BaseCardTest {

    @Test
    void withoutEquipmentHasNoBonus() {
        Permanent cub = addCub(player1);

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isFalse();
    }

    @Test
    void whileEquippedGetsBonusAndFlying() {
        Permanent cub = addCub(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(cub.getId());

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isTrue();
    }

    @Test
    void losesBonusWhenEquipmentIsDetached() {
        Permanent cub = addCub(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(cub.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isFalse();
    }

    private Permanent addCub(Player player) {
        Permanent permanent = new Permanent(new SkyhunterCub());
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
