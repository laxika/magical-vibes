package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PhyrexianManaPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "91")
public class RageExtractor extends Card {

    public RageExtractor() {
        addTrigger(Triggers.youCastSpell().withFilter(new PhyrexianManaPredicate()),
                DealDamageEffect.toAnyTarget(new TargetSpellManaValue()));
    }
}
