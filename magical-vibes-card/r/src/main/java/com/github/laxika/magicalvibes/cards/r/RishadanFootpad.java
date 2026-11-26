package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "94")
public class RishadanFootpad extends Card {

    public RishadanFootpad() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.eachUnless(Subjects.eachOpponent(), new PayManaCost("{2}"),
                        opponents -> new SacrificePermanentsEffect(1, new PermanentTruePredicate(), opponents)));
    }
}
