package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GPT", collectorNumber = "111")
public class Electrolyze extends Card {

    public Electrolyze() {
        // Electrolyze deals 2 damage divided as you choose among one or two targets.
        target(1, 2).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
