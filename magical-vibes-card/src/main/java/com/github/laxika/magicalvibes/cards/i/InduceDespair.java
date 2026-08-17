package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ROE", collectorNumber = "114")
public class InduceDespair extends Card {

    public InduceDespair() {
        addEffect(EffectSlot.SPELL,
                new RevealCardFromHandCost(new CardTypePredicate(CardType.CREATURE), "creature", true));
        addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
