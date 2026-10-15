package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CanBeholdSubtype;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "TDM", collectorNumber = "113")
public class MoltenExhale extends Card {

    public MoltenExhale() {
        setFlashCastCondition(new CanBeholdSubtype(CardSubtype.DRAGON));
        addEffect(EffectSlot.SPELL, BeholdCost.optional(CardSubtype.DRAGON));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
    }
}
