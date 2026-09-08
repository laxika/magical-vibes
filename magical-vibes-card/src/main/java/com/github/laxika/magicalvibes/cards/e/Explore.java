package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "WWK", collectorNumber = "99")
public class Explore extends Card {

    public Explore() {
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
