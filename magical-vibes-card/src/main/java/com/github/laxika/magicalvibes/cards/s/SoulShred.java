package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "POR", collectorNumber = "112")
public class SoulShred extends Card {

    public SoulShred() {
        // Deal 3 damage to a nonblack creature (colorless creatures are nonblack too),
        // then gain 3 life unconditionally.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                "Target must be a nonblack creature."
        ))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
