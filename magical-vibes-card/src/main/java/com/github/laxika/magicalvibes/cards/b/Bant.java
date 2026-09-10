package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "4")
public class Bant extends Card {

    public Bant() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new ConditionalEffect(new AttacksAlone(), new BoostSelfEffect(1, 1)),
                GrantScope.ALL_CREATURES_INCLUDING_SELF));

        PermanentAllOfPredicate eligibleCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE, CardColor.BLUE))));
        target(new PermanentPredicateTargetFilter(
                eligibleCreature,
                "Target must be a green, white, or blue creature"));
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.DIVINITY),
                new GrantStaticEffectToTargetEffect(new GrantKeywordEffect(
                        Keyword.INDESTRUCTIBLE,
                        GrantScope.SELF,
                        new PermanentHasCountersPredicate(CounterType.DIVINITY)))));
    }
}
