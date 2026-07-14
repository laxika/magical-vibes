package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "27")
public class Kismet extends Card {

    public Kismet() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE, CardType.LAND), true));
    }
}
