package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;

/**
 * Benevolent Geist — back face of Malevolent Hermit.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class BenevolentGeist extends Card {

    public BenevolentGeist() {
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect(true));
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
