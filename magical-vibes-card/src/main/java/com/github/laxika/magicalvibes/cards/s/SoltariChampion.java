package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "STH", collectorNumber = "18")
@CardRegistration(set = "TPR", collectorNumber = "29")
public class SoltariChampion extends Card {

    public SoltariChampion() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                1,
                1,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
    }
}
