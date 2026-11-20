package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "181")
@CardRegistration(set = "SOC", collectorNumber = "287")
@CardRegistration(set = "TDC", collectorNumber = "271")
@CardRegistration(set = "OTC", collectorNumber = "208")
@CardRegistration(set = "EOC", collectorNumber = "51")
@CardRegistration(set = "KHC", collectorNumber = "74")
@CardRegistration(set = "ZNC", collectorNumber = "83")
public class SpringbloomDruid extends Card {

    public SpringbloomDruid() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED))),
                "Sacrifice a land?"));
    }
}
