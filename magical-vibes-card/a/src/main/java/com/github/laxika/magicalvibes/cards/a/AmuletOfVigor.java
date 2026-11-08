package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "167")
@CardRegistration(set = "LCC", collectorNumber = "103")
public class AmuletOfVigor extends Card {

    public AmuletOfVigor() {
        // Whenever a permanent you control enters tapped, untap it: "enters tapped" is the permanent as it entered,
        // the trigger's filter; "it" is the stack entry's triggering permanent.
        addTrigger(Triggers.enters(new PermanentIsTappedPredicate(), ObjectRelation.YOU_CONTROL),
                new UntapPermanentsEffect(TapUntapScope.TRIGGERING));
    }
}
