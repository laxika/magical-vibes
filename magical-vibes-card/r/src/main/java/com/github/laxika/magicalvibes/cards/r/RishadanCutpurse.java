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

@CardRegistration(set = "MMQ", collectorNumber = "93")
public class RishadanCutpurse extends Card {

    public RishadanCutpurse() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.eachUnless(Subjects.eachOpponent(), new PayManaCost("{1}"),
                        opponents -> new SacrificePermanentsEffect(1, new PermanentTruePredicate(), opponents)));
    }
}
