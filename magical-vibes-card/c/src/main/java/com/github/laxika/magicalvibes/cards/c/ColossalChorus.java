package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;

@CardRegistration(set = "YLCI", collectorNumber = "18")
public class ColossalChorus extends Card {

    public ColossalChorus() {
        addEffect(EffectSlot.SPELL, new InitializeSourceIntensityEffect(2));
        addEffect(EffectSlot.SPELL,
                new ConjureCardsToBattlefieldEffect("Colossal Dreadmaw", new SourceIntensity()));
        addEffect(EffectSlot.SPELL, new IntensifyCardsOfSubtypeEffect(CardSubtype.CHORUS));
    }
}
