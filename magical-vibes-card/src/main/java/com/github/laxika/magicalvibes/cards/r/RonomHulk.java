package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "CSP", collectorNumber = "119")
public class RonomHulk extends Card {

    public RonomHulk() {
        addEffect(EffectSlot.STATIC, new ProtectionFromPermanentsMatchingEffect(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));
    }
}
