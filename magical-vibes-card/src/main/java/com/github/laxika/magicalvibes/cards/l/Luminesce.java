package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "28")
public class Luminesce extends Card {

    public Luminesce() {
        addEffect(EffectSlot.SPELL, new PreventDamageFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED)));
    }
}
