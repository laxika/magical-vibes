package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "169")
public class PlagueWind extends Card {

    public PlagueWind() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(Set.of(CardType.CREATURE), true, true));
    }
}
