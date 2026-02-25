package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlamebornHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Has must-attack static effect")
    void hasMustAttackEffect() {
        FlamebornHellion card = new FlamebornHellion();

        assertThat(card.getEffects(EffectSlot.STATIC)).singleElement()
                .isInstanceOf(MustAttackEffect.class);
    }
}
