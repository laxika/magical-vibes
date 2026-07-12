package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "8ED", collectorNumber = "82")
public class Hibernation extends Card {

    public Hibernation() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
