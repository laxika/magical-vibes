package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;

@CardRegistration(set = "DOM", collectorNumber = "198")
public class JodahArchmageEternal extends Card {

    public JodahArchmageEternal() {
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect("{W}{U}{B}{R}{G}", null));
    }
}
