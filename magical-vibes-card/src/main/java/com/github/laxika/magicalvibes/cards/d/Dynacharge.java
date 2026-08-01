package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Target creature you control gets +2/+0 until end of turn.
 * <p>
 * Overload {2}{R} (CR 702.96a): paying the overload cost instead of {R} changes "target" to
 * "each", so every creature its controller controls gets +2/+0 and, per CR 702.96b, the spell
 * chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "92")
public class Dynacharge extends Card {

    public Dynacharge() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new BoostTargetCreatureEffect(2, 0),
                new BoostAllCreaturesEffect(2, 0,
                        new PermanentControlledBySourceControllerPredicate())));
        target(TargetFilters.creatureYouControl());
    }
}
