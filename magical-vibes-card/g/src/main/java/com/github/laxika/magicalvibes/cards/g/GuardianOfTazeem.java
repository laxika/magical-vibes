package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "78")
public class GuardianOfTazeem extends Card {

    public GuardianOfTazeem() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new ConditionalEffect(new TriggeringPermanentHasSubtype(CardSubtype.ISLAND),
                                new SkipNextUntapEffect(TapUntapScope.TARGET), false));
    }
}
