package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarfholdChampion.class, ViridianLongbow.class})
class DwarfholdChampionTest extends BaseCardTest {

    @Test
    void withoutEquipmentHasPrintedToughness() {
        Permanent champion = addChampionReady(player1);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    @Test
    void whileEquippedGetsToughnessBoost() {
        Permanent champion = addChampionReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(champion.getId());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void losesToughnessBoostWhenEquipmentIsDetached() {
        Permanent champion = addChampionReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(champion.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    private Permanent addChampionReady(Player player) {
        Permanent permanent = new Permanent(new DwarfholdChampion());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent permanent = new Permanent(new ViridianLongbow());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
