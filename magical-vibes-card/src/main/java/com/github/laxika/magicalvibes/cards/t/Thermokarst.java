package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Thermokarst — {1}{G}{G} Sorcery.
 * Destroy target land. If that land was a snow land, you gain 1 life.
 */
@CardRegistration(set = "ICE", collectorNumber = "268")
public class Thermokarst extends Card {

    public Thermokarst() {
        // Snow check runs while the land is still on the battlefield — before the destroy.
        // Equivalent to the printed "was a snow land" wording (Icequake pattern).
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                        new GainLifeEffect(1)))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
