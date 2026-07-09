package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "52")
public class AquitectsWill extends Card {

    public AquitectsWill() {
        // Put a flood counter on target land. That land is an Island in addition to its other
        // types for as long as it has a flood counter on it. (Modeled as a permanent Island grant,
        // since nothing in the pool removes flood counters.)
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        )).addEffect(EffectSlot.SPELL,
                new GrantBasicLandTypeToTargetEffect(EffectDuration.CONTINUOUS, CardSubtype.ISLAND));
        // If you control a Merfolk, draw a card.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                new DrawCardEffect()));
    }
}
