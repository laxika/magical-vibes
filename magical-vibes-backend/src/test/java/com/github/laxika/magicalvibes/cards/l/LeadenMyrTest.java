package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeadenMyrTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Leaden Myr has tap-for-black mana ability")
    void hasTapForBlackManaAbility() {
        LeadenMyr card = new LeadenMyr();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.BLACK));
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Leaden Myr produces one black mana")
    void tappingProducesBlackMana() {
        Permanent perm = new Permanent(new LeadenMyr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
