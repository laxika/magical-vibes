package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "LCI", collectorNumber = "56")
@CardRegistration(set = "LCI", collectorNumber = "363")
public class TheEverflowingWell extends Card {

    public TheEverflowingWell() {
        setBackFaceCard(new TheMyriadPools());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(2));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new GraveyardCardThreshold(8, new CardIsPermanentPredicate()),
                new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheMyriadPools";
    }
}
