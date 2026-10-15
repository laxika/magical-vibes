package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandDealsDamageToTriggeringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "REX", collectorNumber = "19")
@CardRegistration(set = "REX", collectorNumber = "44")
public class SwoopingPteranodon extends Card {

    public SwoopingPteranodon() {
        target(TargetFilters.creatureAnOpponentControls()).addEffect(
                EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR),
                                new PermanentHasKeywordPredicate(Keyword.FLYING))),
                        SequenceEffect.of(
                                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.HASTE), GrantScope.TARGET),
                                new RegisterDelayedEndStepTriggerEffect(
                                        List.of(new DelayedTargetGroup(TargetFilters.land(), 1, 1)),
                                        new TargetLandDealsDamageToTriggeringCreatureEffect()))));
    }
}
