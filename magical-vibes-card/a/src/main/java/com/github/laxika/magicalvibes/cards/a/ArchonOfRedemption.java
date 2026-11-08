package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "3")
@CardRegistration(set = "SCD", collectorNumber = "7")
public class ArchonOfRedemption extends Card {

    public ArchonOfRedemption() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasKeywordPredicate(Keyword.FLYING))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new MayEffect(new GainLifeEffect(new TriggeringPermanentPower()), "Gain life equal to that creature's power?"));
    }
}
