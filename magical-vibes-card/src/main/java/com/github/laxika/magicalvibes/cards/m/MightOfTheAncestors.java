package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "23")
public class MightOfTheAncestors extends Card {

    public MightOfTheAncestors() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET,
                                new PermanentIsCreaturePredicate()));
    }
}
