package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraveEndeavorEffect;

@CardRegistration(set = "AFC", collectorNumber = "24")
public class GraveEndeavor extends Card {

    public GraveEndeavor() {
        addEffect(EffectSlot.SPELL, new GraveEndeavorEffect());
    }
}
