package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;

@CardRegistration(set = "JUD", collectorNumber = "51")
public class Spelljack extends Card {

    public Spelljack() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileThenGrantFreeCastPermissionEffect());
    }
}
