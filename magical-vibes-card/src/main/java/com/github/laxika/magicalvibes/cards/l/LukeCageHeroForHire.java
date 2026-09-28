package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MSC", collectorNumber = "696")
public class LukeCageHeroForHire extends Card {

    public LukeCageHeroForHire() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, CreateTokenEffect.ofTreasureToken(1));
    }
}
