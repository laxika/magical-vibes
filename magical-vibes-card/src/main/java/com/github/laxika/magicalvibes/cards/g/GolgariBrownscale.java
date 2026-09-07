package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RAV", collectorNumber = "166")
public class GolgariBrownscale extends Card {

    public GolgariBrownscale() {
        addEffect(EffectSlot.GRAVEYARD_ON_SELF_RETURNED_TO_HAND, new GainLifeEffect(2));
        addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT, new DredgeEffect(2));
    }
}
