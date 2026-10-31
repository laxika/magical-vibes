package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "WAR", collectorNumber = "132")
public class HonorTheGodPharaoh extends Card {

    public HonorTheGodPharaoh() {
        addCastingCost(new DiscardCardTypeCost(null, null));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new AmassGoblinsEffect(1, CardSubtype.ZOMBIE));
    }
}
