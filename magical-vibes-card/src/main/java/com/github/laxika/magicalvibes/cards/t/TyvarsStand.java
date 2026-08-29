package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "190")
public class TyvarsStand extends Card {

    public TyvarsStand() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new XValue(), new XValue()))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                        Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.TARGET));
    }
}
