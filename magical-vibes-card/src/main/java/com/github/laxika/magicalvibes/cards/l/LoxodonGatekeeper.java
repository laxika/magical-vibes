package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "25")
public class LoxodonGatekeeper extends Card {

    public LoxodonGatekeeper() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE, CardType.LAND), true));
    }
}
