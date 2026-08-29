package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "162")
public class ScrappyBruiser extends Card {

    public ScrappyBruiser() {
        target(TargetFilters.attackingCreature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ATTACK, new ReturnTargetPermanentToHandAtEndOfCombatEffect());
    }
}
