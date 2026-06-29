package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopperMyrTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Copper Myr has tap-for-green mana ability")
    void hasTapForGreenManaAbility() {
        CopperMyr card = new CopperMyr();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.GREEN));
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Copper Myr produces one green mana")
    void tappingProducesGreenMana() {
        Permanent perm = new Permanent(new CopperMyr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
