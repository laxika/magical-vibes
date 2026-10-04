package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "FDC", collectorNumber = "231")
public class SurrakAndGoreclaw extends Card {

    public SurrakAndGoreclaw() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT)));
    }
}
