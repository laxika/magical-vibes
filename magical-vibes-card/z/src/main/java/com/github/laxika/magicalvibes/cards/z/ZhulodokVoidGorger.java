package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "704")
@CardRegistration(set = "CMM", collectorNumber = "752")
@CardRegistration(set = "CMM", collectorNumber = "779")
public class ZhulodokVoidGorger extends Card {

    public ZhulodokVoidGorger() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardIsColorlessPredicate(),
                                new CardMinManaValuePredicate(7, true))))
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.HAND))
                        .grantedToTheSpell(),
                new CascadeEffect(),
                new CascadeEffect());
    }
}
