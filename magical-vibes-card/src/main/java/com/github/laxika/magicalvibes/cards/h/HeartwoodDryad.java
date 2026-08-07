package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockCreaturesWithShadowEffect;

/**
 * Heartwood Dryad — 2/1 Dryad that can block creatures with shadow as though it had shadow.
 */
@CardRegistration(set = "TMP", collectorNumber = "231")
public class HeartwoodDryad extends Card {

    public HeartwoodDryad() {
        addEffect(EffectSlot.STATIC, new CanBlockCreaturesWithShadowEffect());
    }
}
