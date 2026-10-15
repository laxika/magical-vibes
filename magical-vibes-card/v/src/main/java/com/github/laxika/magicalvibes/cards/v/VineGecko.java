package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "219")
public class VineGecko extends Card {

    public VineGecko() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForFirstMatchingSpellEachTurnEffect(
                new CardTruePredicate(), 1, true));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new KickedSpellCastTriggerEffect(
                List.of(new PutCountersOnSourceEffect(1, 1, 1))));
    }
}
