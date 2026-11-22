package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "193")
@CardRegistration(set = "ECL", collectorNumber = "367")
@CardRegistration(set = "ECL", collectorNumber = "391")
@CardRegistration(set = "ECL", collectorNumber = "401")
public class SelflessSafewright extends Card {

    public SelflessSafewright() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCreatureTypeAtResolutionEffect(),
                new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE),
                        Subjects.otherPermanentsYouControl(), new PermanentHasTypeChosenThisResolutionPredicate()));
    }
}
