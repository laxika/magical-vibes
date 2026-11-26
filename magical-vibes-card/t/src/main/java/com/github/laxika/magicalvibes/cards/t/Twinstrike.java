package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "134")
public class Twinstrike extends Card {

    public Twinstrike() {
        target(TargetFilters.creature(), 2, 2).addSpellEffect(new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                DealDamageEffect.toEachTarget(new Fixed(2)),
                DestroyTargetPermanentEffect.eachTarget()
        ));
    }
}
