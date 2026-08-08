package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Target creature you control gets +1/+0 and gains first strike until end of turn.
 * <p>
 * Overload {1}{R} (CR 702.96a): paying the overload cost instead of {R} changes "target" to
 * "each", so every creature its controller controls gets the pump and first strike and, per
 * CR 702.96b, the spell chooses no targets at all.
 */
@CardRegistration(set = "DGM", collectorNumber = "40")
public class WeaponSurge extends Card {

    public WeaponSurge() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new BoostTargetCreatureEffect(1, 0),
                new BoostAllOwnCreaturesEffect(1, 0)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES)));
        target(TargetFilters.creatureYouControl());
    }
}
