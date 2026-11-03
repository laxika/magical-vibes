package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "246")
@CardRegistration(set = "ECL", collectorNumber = "343")
public class ThoughtweftLieutenant extends Card {

    public ThoughtweftLieutenant() {
        // Whenever this creature or another Kithkin you control enters, target creature you control
        // gets +1/+1 and gains trample until end of turn.
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentHasSubtypePredicate(CardSubtype.KITHKIN))),
                                ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                        SequenceEffect.of(
                                new BoostTargetCreatureEffect(1, 1),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));
    }
}
