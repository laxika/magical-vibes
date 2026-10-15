package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YDSK", collectorNumber = "28")
public class ValiantEmberkin extends Card {

    public ValiantEmberkin() {
        var target = target(TargetFilters.creatureYouControl());
        target.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyBoostTargetCreatureEffect(1, 0));
        target.addEffect(EffectSlot.ON_ATTACK,
                new PerpetuallyBoostTargetCreatureEffect(1, 0));

        addEffect(EffectSlot.STATIC,
                AdditionalTriggeredAbilityEffect.forAllyCreatureBecomesTarget());
    }
}
