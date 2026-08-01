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
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Target creature you don't control gets -4/-0 until end of turn.
 * <p>
 * Overload {2}{U} (CR 702.96a): paying the overload cost instead of {U} changes "target" to
 * "each", so every creature its controller doesn't control gets -4/-0 and, per CR 702.96b,
 * the spell chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "38")
public class Downsize extends Card {

    public Downsize() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new BoostTargetCreatureEffect(-4, 0),
                new BoostAllCreaturesEffect(-4, 0,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
