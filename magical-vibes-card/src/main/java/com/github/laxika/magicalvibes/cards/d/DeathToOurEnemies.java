package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "127")
public class DeathToOurEnemies extends Card {

    public DeathToOurEnemies() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(SequenceEffect.of(
                        CreateTokenEffect.ofTappedTreasureToken(1),
                        new PutCountersOnSelfEffect(CounterType.PLAN)))
        ));

        PermanentPredicate anyTargetPermanent = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                anyTargetPermanent,
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, or player");
        DealDividedDamageEffect damage = new DealDividedDamageEffect(
                new Fixed(7), null, DivisionMode.CHOSEN, anyTargetPermanent,
                2, true, false, true);

        target(anyTarget, 1, 2).addEffect(EffectSlot.ON_SELF_COUNTERS_PUT,
                new ConditionalEffect(
                        new SourceCounterThreshold(4, CounterType.PLAN),
                        SacrificeSelfThenEffect.reflexive(damage)));
    }
}
