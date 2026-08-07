package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "GTC", collectorNumber = "204")
public class UrbanEvolution extends Card {

    public UrbanEvolution() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
    }
}
