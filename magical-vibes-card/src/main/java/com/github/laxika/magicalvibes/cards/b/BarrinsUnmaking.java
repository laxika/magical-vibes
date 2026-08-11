package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesMostCommonColorPredicate;

/**
 * Barrin's Unmaking — return a target permanent if it shares a color with a most-common color.
 */
@CardRegistration(set = "INV", collectorNumber = "46")
public class BarrinsUnmaking extends Card {

    public BarrinsUnmaking() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetPermanentMatches(new PermanentSharesMostCommonColorPredicate()),
                ReturnToHandEffect.target()));
    }
}
