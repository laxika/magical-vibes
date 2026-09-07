package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CrystalSlipper.class, GrizzlyBears.class})
class CrystalSlipperTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and haste")
    void equippedCreatureGetsBoostAndHaste() {
        Permanent slipper = addReadySlipper(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        slipper.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unattached Crystal Slipper does not affect creatures")
    void unattachedSlipperDoesNotAffectCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addReadySlipper(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip ability attaches Crystal Slipper to a creature")
    void equipAttachesSlipper() {
        Permanent slipper = addReadySlipper(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(slipper.getAttachedTo()).isEqualTo(bears.getId());
    }

    private Permanent addReadySlipper(Player player) {
        Permanent slipper = new Permanent(new CrystalSlipper());
        slipper.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(slipper);
        return slipper;
    }
}
