package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;

@CardRegistration(set = "YLCI", collectorNumber = "18")
public class ColossalChorus extends Card {

    public ColossalChorus() {
        addSpellEffect(new InitializeSourceIntensityEffect(2));
        addSpellEffect(ConjureCardsEffect.named("XLN", "180", ConjureDestination.ontoBattlefield()).times(new SourceIntensity()));
        addSpellEffect(new IntensifyCardsOfSubtypeEffect(CardSubtype.CHORUS));
    }
}
