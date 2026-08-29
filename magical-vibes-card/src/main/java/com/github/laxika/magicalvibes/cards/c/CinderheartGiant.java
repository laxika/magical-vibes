package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentCreatureEffect;

@CardRegistration(set = "KHM", collectorNumber = "126")
public class CinderheartGiant extends Card {

    public CinderheartGiant() {
        // When this creature dies, it deals 7 damage to a creature an opponent controls chosen at random.
        addEffect(EffectSlot.ON_DEATH, new DealDamageToRandomOpponentCreatureEffect(7));
    }
}
