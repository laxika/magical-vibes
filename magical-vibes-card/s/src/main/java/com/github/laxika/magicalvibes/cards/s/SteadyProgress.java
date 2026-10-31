package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SOM", collectorNumber = "45")
@CardRegistration(set = "MM2", collectorNumber = "58")
public class SteadyProgress extends Card {

    public SteadyProgress() {
        addSpellEffect(new ProliferateEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
