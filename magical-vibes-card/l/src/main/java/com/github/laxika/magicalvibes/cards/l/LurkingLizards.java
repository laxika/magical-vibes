package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "107")
@CardRegistration(set = "OM1", collectorNumber = "109")
@CardRegistration(set = "MSC", collectorNumber = "816")
public class LurkingLizards extends Card {

    public LurkingLizards() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new PutCountersOnSourceEffect(1, 1, 1)),
                new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(3))
        ));
    }
}
