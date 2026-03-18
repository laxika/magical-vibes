package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "99")
public class OgreGeargrabber extends Card {

    public OgreGeargrabber() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be an Equipment an opponent controls"
        ))
                .addEffect(EffectSlot.ON_ATTACK, new GainControlOfTargetPermanentUntilEndOfTurnEffect())
                .addEffect(EffectSlot.ON_ATTACK, new AttachTargetToSourcePermanentEffect());
    }
}
