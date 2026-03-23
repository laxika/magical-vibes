package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "19")
public class KinjallisSunwing extends Card {

    public KinjallisSunwing() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(Set.of(CardType.CREATURE), true));
    }
}
