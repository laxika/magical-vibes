package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "41")
@CardRegistration(set = "PIO", collectorNumber = "56")
public class GuildSummit extends Card {
    public GuildSummit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                                new PermanentAllOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.GATE),
                                        new PermanentCanBecomeTappedPredicate()))),
                        "any number of untapped Gates you control to tap"),
                new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)),
                new DrawCardEffect(new AmountThisWay(StepKind.TAP), Subjects.you())));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentHasSubtypePredicate(CardSubtype.GATE))),
                        ObjectRelation.YOU_CONTROL),
                new DrawCardEffect());
    }
}
