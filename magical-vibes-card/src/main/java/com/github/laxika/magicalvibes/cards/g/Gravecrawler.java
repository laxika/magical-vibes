package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "64")
public class Gravecrawler extends Card {

    public Gravecrawler() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addCastingOption(new GraveyardCast(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
    }
}
