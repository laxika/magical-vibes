package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BeholdCostPaid;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YTDM", collectorNumber = "27")
public class TerritorialStrike extends Card {

    public TerritorialStrike() {
        addEffect(EffectSlot.SPELL, BeholdCost.optional(CardSubtype.DRAGON));
        var nonlandPermanent = TargetFilters.nonlandPermanent();
        target(nonlandPermanent).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new BeholdCostPaid(),
                new DestroyTargetPermanentEffect(nonlandPermanent.predicate()),
                new DestroyTargetPermanentThenPerpetuallyBoostEffect(2, 2, nonlandPermanent.predicate())
        ));
    }
}
