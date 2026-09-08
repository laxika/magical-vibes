package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CSP", collectorNumber = "141")
public class PhyrexianSoulgorger extends Card {

    public PhyrexianSoulgorger() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CumulativeUpkeepEffect.sacrifice(new PermanentIsCreaturePredicate()));
    }
}
