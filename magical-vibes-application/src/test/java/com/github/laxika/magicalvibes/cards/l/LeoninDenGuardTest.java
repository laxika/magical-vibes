package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfiltrationLens;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninDenGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Unequipped Leonin Den-Guard has no bonus")
    void unequippedHasNoBonus() {
        Permanent guard = addGuardReady(player1);

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equipped Leonin Den-Guard gets +1/+1 and vigilance")
    void equippedGetsBonusAndVigilance() {
        Permanent guard = addGuardReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(guard.getId());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Leonin Den-Guard loses its bonus when it is no longer equipped")
    void losesBonusWhenUnequipped() {
        Permanent guard = addGuardReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(guard.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addGuardReady(Player player) {
        Permanent perm = new Permanent(new LeoninDenGuard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent perm = new Permanent(new InfiltrationLens());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
