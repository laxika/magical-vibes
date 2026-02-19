package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "235")
public class Smash extends Card {

    public Smash() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
