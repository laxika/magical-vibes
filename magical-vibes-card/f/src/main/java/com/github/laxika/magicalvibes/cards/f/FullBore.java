package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentCastForWarpCostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "135")
public class FullBore extends Card {

    public FullBore() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 2))
                .addEffect(EffectSlot.SPELL, GrantKeywordEffect.toTargetIf(
                        Keyword.TRAMPLE, new PermanentCastForWarpCostPredicate()))
                .addEffect(EffectSlot.SPELL, GrantKeywordEffect.toTargetIf(
                        Keyword.HASTE, new PermanentCastForWarpCostPredicate()));
    }
}
