package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "28")
public class ValiantEmberkin extends Card {

    public ValiantEmberkin() {
        var target = target(TargetFilters.creatureYouControl());
        target.addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyBoostTargetCreatureEffect(1, 0));
        target.addTrigger(Triggers.attacks(),
                new PerpetuallyBoostTargetCreatureEffect(1, 0));

        addStaticEffect(AdditionalTriggeredAbilityEffect.forAllyCreatureBecomesTarget());
    }
}
