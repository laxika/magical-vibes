package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IronMyrTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Iron Myr has tap-for-red mana ability")
    void hasTapForRedManaAbility() {
        IronMyr card = new IronMyr();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.RED));
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Iron Myr produces one red mana")
    void tappingProducesRedMana() {
        Permanent perm = new Permanent(new IronMyr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
