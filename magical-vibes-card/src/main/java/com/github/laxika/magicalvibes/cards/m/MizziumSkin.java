package com.github.laxika.magicalvibes.cards.m;

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
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Target creature you control gets +0/+1 and gains hexproof until end of turn.
 * <p>
 * Overload {1}{U} (CR 702.96a): paying the overload cost instead of {U} changes "target" to
 * "each", so every creature its controller controls is pumped and gains hexproof; per CR 702.96b
 * the overloaded spell chooses no targets.
 */
@CardRegistration(set = "RTR", collectorNumber = "45")
public class MizziumSkin extends Card {

    public MizziumSkin() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                SequenceEffect.of(
                        new BoostTargetCreatureEffect(0, 1),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET)),
                SequenceEffect.of(
                        new BoostAllOwnCreaturesEffect(0, 1),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_CREATURES))));
        target(TargetFilters.creatureYouControl());
    }
}
