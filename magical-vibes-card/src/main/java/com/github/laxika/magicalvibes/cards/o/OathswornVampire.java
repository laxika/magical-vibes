package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "RIX", collectorNumber = "80")
public class OathswornVampire extends Card {

    public OathswornVampire() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addCastingOption(new GraveyardCast(new GainedLifeThisTurn()));
    }
}
