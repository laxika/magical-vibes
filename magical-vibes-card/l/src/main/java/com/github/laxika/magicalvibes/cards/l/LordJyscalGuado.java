package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnCreatureThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "FIC", collectorNumber = "23")
@CardRegistration(set = "FIC", collectorNumber = "137")
public class LordJyscalGuado extends Card {

    public LordJyscalGuado() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new PutCounterOnCreatureThisTurn(),
                CreateTokenEffect.ofClueToken(1)));
    }
}
