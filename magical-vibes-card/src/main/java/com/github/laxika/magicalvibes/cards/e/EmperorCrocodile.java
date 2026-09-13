package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "9ED", collectorNumber = "241")
@CardRegistration(set = "8ED", collectorNumber = "246")
@CardRegistration(set = "UDS", collectorNumber = "105")
public class EmperorCrocodile extends Card {

    public EmperorCrocodile() {
        // "When you control no other creatures, sacrifice this creature." —
        // State-triggered ability (MTG rule 603.8). The crocodile itself is excluded.
        addEffect(EffectSlot.STATE_TRIGGERED, StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                List.of(new SacrificeSelfEffect()),
                "Emperor Crocodile's state-triggered ability"
        ));
    }
}
