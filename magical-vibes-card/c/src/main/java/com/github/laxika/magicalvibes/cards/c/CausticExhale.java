package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "TDM", collectorNumber = "74")
public class CausticExhale extends Card {

    public CausticExhale() {
        addEffect(EffectSlot.SPELL, BeholdCost.orPayMana(CardSubtype.DRAGON, "{1}"));
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-3, -3));
    }
}
