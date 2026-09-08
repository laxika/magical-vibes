package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "127")
public class TorchBreath extends Card {

    public TorchBreath() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)), 2));
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue()));
    }
}
