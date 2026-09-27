package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "25")
public class ArahboRoarOfTheWorld extends Card {

    public ArahboRoarOfTheWorld() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.CAT),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another Cat you control"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new AnyOf(List.of(new SourceCardInCommandZone(), new SourceCardOnBattlefield())),
                        new BoostTargetCreatureEffect(3, 3)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.CAT),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )),
                        new MayPayManaEffect(
                                "{1}{G}{W}",
                                SequenceEffect.of(
                                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                                        new BoostTargetCreatureEffect(new TargetPower(), new TargetPower())),
                                "Pay {1}{G}{W} to give it trample and +X/+X until end of turn?")));
    }
}
