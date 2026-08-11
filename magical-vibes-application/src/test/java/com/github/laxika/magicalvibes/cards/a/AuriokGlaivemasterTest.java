package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.InfiltrationLens;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuriokGlaivemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Without equipment, Auriok Glaivemaster is a 1/1 without first strike")
    void withoutEquipmentHasNoBonus() {
        Permanent glaivemaster = addGlaivemasterReady(player1);

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("With equipment attached, Auriok Glaivemaster gets +1/+1 and first strike")
    void withEquipmentHasBonus() {
        Permanent glaivemaster = addGlaivemasterReady(player1);
        Permanent lens = addLensReady(player1);
        lens.setAttachedTo(glaivemaster.getId());

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("When equipment is detached, Auriok Glaivemaster loses the bonus")
    void afterEquipmentDetachedLosesBonus() {
        Permanent glaivemaster = addGlaivemasterReady(player1);
        Permanent lens = addLensReady(player1);
        lens.setAttachedTo(glaivemaster.getId());

        lens.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glaivemaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glaivemaster, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addGlaivemasterReady(Player player) {
        Permanent perm = new Permanent(new AuriokGlaivemaster());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLensReady(Player player) {
        Permanent perm = new Permanent(new InfiltrationLens());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
