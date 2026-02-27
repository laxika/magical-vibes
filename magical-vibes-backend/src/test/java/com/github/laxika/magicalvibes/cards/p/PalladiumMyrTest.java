package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PalladiumMyrTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Palladium Myr has tap-for-two-colorless mana effect")
    void hasTapForColorlessManaAbility() {
        PalladiumMyr card = new PalladiumMyr();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.COLORLESS, 2));
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Palladium Myr produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        Permanent perm = new Permanent(new PalladiumMyr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
