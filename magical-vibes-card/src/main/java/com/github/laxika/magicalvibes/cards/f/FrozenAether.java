package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "54")
public class FrozenAether extends Card {

    public FrozenAether() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE, CardType.LAND), true));
    }
}
