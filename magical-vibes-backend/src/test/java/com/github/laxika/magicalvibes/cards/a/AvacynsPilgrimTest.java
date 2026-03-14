package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvacynsPilgrimTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Avacyn's Pilgrim has tap-for-white mana ability")
    void hasTapForWhiteManaAbility() {
        AvacynsPilgrim card = new AvacynsPilgrim();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.WHITE));
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Avacyn's Pilgrim produces one white mana")
    void tappingProducesWhiteMana() {
        Permanent perm = new Permanent(new AvacynsPilgrim());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
