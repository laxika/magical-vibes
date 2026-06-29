package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Have a Bite — the prepare spell (inset) of Adventurous Eater // Have a Bite (SOS 72).
 * <p>
 * Sorcery: Put a +1/+1 counter on target creature. You gain 1 life.
 * <p>
 * Not independently registered: its oracle data is registered for the class name "HaveABite" when
 * Adventurous Eater (SOS 72) loads (see {@code AdventurousEaterHaveABite#getBackFaceClassName}). A copy of
 * this spell is created in exile while Adventurous Eater is prepared and may be cast from there.
 */
public class HaveABite extends Card {

    public HaveABite() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1))
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
    }
}
