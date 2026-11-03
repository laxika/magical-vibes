package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "56")
@CardRegistration(set = "LCI", collectorNumber = "363")
public class TheEverflowingWell extends Card {

    public TheEverflowingWell() {
        setBackFaceCard(new TheMyriadPools());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.CONTROLLER));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(2));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new GraveyardCardThreshold(8, new CardIsPermanentPredicate())),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "TheMyriadPools";
    }
}
