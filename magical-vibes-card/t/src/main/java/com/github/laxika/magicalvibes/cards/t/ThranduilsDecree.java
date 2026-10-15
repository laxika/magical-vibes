package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;

@CardRegistration(set = "HOB", collectorNumber = "56")
public class ThranduilsDecree extends Card {

    public ThranduilsDecree() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileThenGrantFreeCastPermissionEffect(true));
    }
}
