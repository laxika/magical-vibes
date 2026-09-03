package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "SNC", collectorNumber = "179")
public class CorpseExplosion extends Card {

    public CorpseExplosion() {
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE, false, false, true));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new XValue(), false, true, null));
    }
}
