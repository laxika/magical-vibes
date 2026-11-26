package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "70")
@CardRegistration(set = "DDK", collectorNumber = "22")
@CardRegistration(set = "E02", collectorNumber = "21")
@CardRegistration(set = "VOC", collectorNumber = "139")
public class UrgeToFeed extends Card {
    public UrgeToFeed() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(-3, -3));
        addSpellEffect(ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                new PermanentCanBecomeTappedPredicate()))),
                "any number of untapped Vampires you control to tap"));
        addSpellEffect(new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutCountersEffect(Subjects.thisWay(StepKind.TAP),
                CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
