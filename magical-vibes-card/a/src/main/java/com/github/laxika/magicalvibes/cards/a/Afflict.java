package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "125")
@CardRegistration(set = "ODY", collectorNumber = "115")
public class Afflict extends Card {

    public Afflict() {
        addSpellEffect(new BoostTargetCreatureEffect(-1, -1));
        addSpellEffect(new DrawCardEffect());
    }
}
