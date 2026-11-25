package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "245")
@CardRegistration(set = "BRC", collectorNumber = "102")
@CardRegistration(set = "ELD", collectorNumber = "318")
public class WorkshopElders extends Card {

    public WorkshopElders() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.FLYING,
                Subjects.creaturesYouControl(),
                new PermanentIsArtifactPredicate()));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                )),
                "Target must be a noncreature artifact you control"
        )).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new AnimatePermanentsEffect(
                                0,
                                0,
                                List.of(),
                                Set.of(),
                                null,
                                Set.of(CardType.CREATURE),
                                Subjects.target(),
                                Duration.PERMANENT),
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)
                ),
                "Have target noncreature artifact you control become a 0/0 artifact creature?"
        ));
    }
}
