package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ELD", collectorNumber = "32")
public class SyrAlinTheLionsClaw extends Card {

    public SyrAlinTheLionsClaw() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                1, 1, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
    }
}
