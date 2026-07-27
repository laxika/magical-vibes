package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "170")
public class Anarchy extends Card {

    public Anarchy() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
