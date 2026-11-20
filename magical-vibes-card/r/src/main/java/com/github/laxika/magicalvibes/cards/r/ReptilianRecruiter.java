package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "149")
public class ReptilianRecruiter extends Card {

    public ReptilianRecruiter() {
        // When this creature enters, choose target creature. If that creature's power is 2 or less
        // or if you control another Lizard, gain control of that creature until end of turn, untap
        // it, and it gains haste until end of turn.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(
                        new AnyOf(List.of(
                                new TargetPermanentMatches(new PermanentPowerAtMostPredicate(2)),
                                new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.LIZARD))
                        )),
                        SequenceEffect.of(
                                new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                                new UntapPermanentsEffect(Subjects.target()),
                                new GrantKeywordEffect(Keyword.HASTE, Subjects.target())
                        )));
    }
}
