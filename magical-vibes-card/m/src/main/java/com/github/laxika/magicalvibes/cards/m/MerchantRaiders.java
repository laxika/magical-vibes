package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "163")
public class MerchantRaiders extends Card {

    public MerchantRaiders() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                        new TriggeringCardConditionalEffect(
                                new CardAnyOfPredicate(List.of(new CardIsSelfPredicate(),
                                        new CardSubtypePredicate(CardSubtype.PIRATE))),
                                SequenceEffect.of(
                                        new TapPermanentsEffect(TapUntapScope.TARGET),
                                        DoesntUntapEffect.targetWhileSourceControlled())));
    }
}
