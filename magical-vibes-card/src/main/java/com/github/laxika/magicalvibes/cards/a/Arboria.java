package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessDefendingPlayerActedLastTurnEffect;

@CardRegistration(set = "LEG", collectorNumber = "174")
public class Arboria extends Card {

    public Arboria() {
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackUnlessDefendingPlayerActedLastTurnEffect());
    }
}
