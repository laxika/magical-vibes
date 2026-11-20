package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "88")
@CardRegistration(set = "2XM", collectorNumber = "85")
@CardRegistration(set = "C14", collectorNumber = "140")
@CardRegistration(set = "NCC", collectorNumber = "247")
@CardRegistration(set = "TDC", collectorNumber = "178")
@CardRegistration(set = "C20", collectorNumber = "132")
public class DiscipleOfBolas extends Card {

    public DiscipleOfBolas() {
        // When this creature enters, sacrifice another creature. You gain X life and draw X cards,
        // where X is that creature's power.
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))), Subjects.you()),
                new GainLifeEffect(AmountThisWay.sacrificedPower()),
                new DrawCardEffect(AmountThisWay.sacrificedPower())));
    }
}
