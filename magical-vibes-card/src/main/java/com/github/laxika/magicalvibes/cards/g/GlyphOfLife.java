package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "15")
public class GlyphOfLife extends Card {

    public GlyphOfLife() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.WALL),
                "Target must be a Wall"
        )).addEffect(EffectSlot.SPELL,
                new RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect(
                        List.of(new GainLifeEffect(new EventValue()))));
    }
}
