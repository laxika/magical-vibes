package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "23")
public class LanternFlare extends Card {

    public LanternFlare() {
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addCastingOption(AlternateHandCast.cleave("{X}{R}{W}", creatureOrPlaneswalker()));
        target(creatureOrPlaneswalker())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CastForAlternateCost(),
                        SequenceEffect.of(
                                new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue()),
                                new GainLifeEffect(new XValue()))))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(new CastForAlternateCost()),
                        SequenceEffect.of(
                                new DealDamageToTargetCreatureOrPlaneswalkerEffect(creaturesYouControl),
                                new GainLifeEffect(creaturesYouControl))));
    }

    private static PermanentPredicateTargetFilter creatureOrPlaneswalker() {
        return new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                "Target must be a creature or planeswalker");
    }
}
