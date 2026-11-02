package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "32")
@CardRegistration(set = "OPCA", collectorNumber = "67")
public class SanctumOfSerra extends Card {

    public SanctumOfSerra() {
        addTrigger(Triggers.planeswalkAwayFromThis(),
                new DestroyAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addTrigger(Triggers.chaosEnsues(),
                new MayEffect(new SetLifeTotalEffect(20), "Have your life total become 20?"));
    }
}
