package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageBySelfToCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "CSP", collectorNumber = "82")
public class GoblinFurrier extends Card {

    public GoblinFurrier() {
        addEffect(EffectSlot.STATIC, new PreventDamageBySelfToCreaturesEffect(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)));
    }
}
