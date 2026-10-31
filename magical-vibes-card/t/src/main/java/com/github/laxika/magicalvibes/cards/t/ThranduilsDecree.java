package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;

@CardRegistration(set = "HOB", collectorNumber = "56")
public class ThranduilsDecree extends Card {

    public ThranduilsDecree() {
        addSpellEffect(new CounterSpellAndExileThenGrantFreeCastPermissionEffect(true));
    }
}
