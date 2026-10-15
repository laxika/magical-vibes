package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "IKO", collectorNumber = "173")
public class ThwartTheEnemy extends Card {

    public ThwartTheEnemy() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allByOpponentCreatures());
    }
}
