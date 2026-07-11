package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackControllerNextTurnEffect;

@CardRegistration(set = "POR", collectorNumber = "71")
public class Taunt extends Card {

    public Taunt() {
        // During target player's next turn, creatures that player controls attack you if able.
        addEffect(EffectSlot.SPELL, new MustAttackControllerNextTurnEffect());
    }
}
