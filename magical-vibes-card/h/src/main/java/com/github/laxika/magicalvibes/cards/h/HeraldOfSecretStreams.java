package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "XLN", collectorNumber = "59")
@CardRegistration(set = "LCC", collectorNumber = "158")
public class HeraldOfSecretStreams extends Card {

    public HeraldOfSecretStreams() {
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new CantBeBlockedEffect(),
                Subjects.creaturesYouControl(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
