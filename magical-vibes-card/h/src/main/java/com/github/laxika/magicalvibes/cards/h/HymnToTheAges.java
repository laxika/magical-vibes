package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "6")
public class HymnToTheAges extends Card {

    public HymnToTheAges() {
        setSubtypes(List.of(CardSubtype.CHORUS));
        addEffect(EffectSlot.SPELL, new InitializeSourceIntensityEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new SourceIntensity()));
        addEffect(EffectSlot.SPELL, new IntensifyCardsOfSubtypeEffect(CardSubtype.CHORUS));
    }
}
