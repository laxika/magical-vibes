package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;

@CardRegistration(set = "YLCI", collectorNumber = "17")
public class RibaldShanty extends Card {

    public RibaldShanty() {
        addEffect(EffectSlot.SPELL, new InitializeSourceIntensityEffect(2));
        addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(new SourceIntensity()));
        addEffect(EffectSlot.SPELL, new IntensifyCardsOfSubtypeEffect(CardSubtype.CHORUS));
    }
}
