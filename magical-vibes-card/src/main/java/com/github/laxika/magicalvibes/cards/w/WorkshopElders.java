package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "245")
@CardRegistration(set = "BRC", collectorNumber = "102")
public class WorkshopElders extends Card {

    public WorkshopElders() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsArtifactPredicate()));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                )),
                "Target must be a noncreature artifact you control"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new AnimatePermanentsEffect(
                                0,
                                0,
                                List.of(),
                                Set.of(),
                                null,
                                Set.of(CardType.CREATURE),
                                GrantScope.TARGET,
                                EffectDuration.PERMANENT),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)
                ),
                "Have target noncreature artifact you control become a 0/0 artifact creature?"
        ));
    }
}
