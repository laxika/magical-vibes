package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "64")
@CardRegistration(set = "MKM", collectorNumber = "395")
public class LostInTheMaze extends Card {

    public LostInTheMaze() {
        targetWithDynamicCount(new XValue(), TargetFilters.creature(), Integer.MAX_VALUE)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(Subjects.target()),
                        PutCounterOnTargetPermanentEffect.withResolutionCondition(
                                CounterType.STUN, 1,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.creaturesYouControl(),
                        new PermanentIsTappedPredicate()));
    }
}
