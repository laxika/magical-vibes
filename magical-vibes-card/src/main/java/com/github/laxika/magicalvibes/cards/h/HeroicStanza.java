package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Heroic Stanza — the prepare spell (inset) of Abigale, Poet Laureate // Heroic Stanza (SOS 170).
 * <p>
 * Sorcery: Put a +1/+1 counter on target creature.
 * <p>
 * Not independently registered: its oracle data is registered for the class name "HeroicStanza" when
 * Abigale (SOS 170) loads (see {@code AbigalePoetLaureateHeroicStanza#getBackFaceClassName}). A copy of
 * this spell is created in exile while Abigale is prepared and may be cast from there.
 */
public class HeroicStanza extends Card {

    public HeroicStanza() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1));
    }
}
