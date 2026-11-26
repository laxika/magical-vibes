package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaSpentLessThanManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "171")
@CardRegistration(set = "TMT", collectorNumber = "252")
public class TokkaRahzarTerribleTwos extends Card {

    public TokkaRahzarTerribleTwos() {
        addStaticEffect(new CantBeCounteredEffect());
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryManaSpentLessThanManaValuePredicate()),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 3));
    }
}
