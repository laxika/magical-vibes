package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "337")
@CardRegistration(set = "MOM", collectorNumber = "380")
public class SurrakAndGoreclaw extends Card {

    public SurrakAndGoreclaw() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(1, false,
                        List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))));
    }
}
