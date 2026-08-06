package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockCreaturesWithShadowEffect;

/**
 * Wall of Diffusion — 0/5 Wall with defender that can block creatures with shadow
 * as though it had shadow. Defender comes from Scryfall keywords.
 */
@CardRegistration(set = "TMP", collectorNumber = "211")
public class WallOfDiffusion extends Card {

    public WallOfDiffusion() {
        addEffect(EffectSlot.STATIC, new CanBlockCreaturesWithShadowEffect());
    }
}
