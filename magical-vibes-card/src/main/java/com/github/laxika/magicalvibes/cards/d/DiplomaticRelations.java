package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "177")
public class DiplomaticRelations extends Card {

    public DiplomaticRelations() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 0))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new TargetDealsPowerDamageToTargetEffect());
    }
}
