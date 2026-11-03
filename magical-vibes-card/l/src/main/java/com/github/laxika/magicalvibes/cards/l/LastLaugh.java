package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "68")
public class LastLaugh extends Card {

    public LastLaugh() {
        addTrigger(Triggers.dies(null, ObjectRelation.ANY),
                new MassDamageEffect(1, true));

        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentIsCreaturePredicate()), new SacrificeSelfEffect());
    }
}
