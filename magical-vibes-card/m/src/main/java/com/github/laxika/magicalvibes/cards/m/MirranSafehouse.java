package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsInAllGraveyardsEffect;

@CardRegistration(set = "ONE", collectorNumber = "232")
public class MirranSafehouse extends Card {

    public MirranSafehouse() {
        addStaticEffect(new GainActivatedAbilitiesOfCardsInAllGraveyardsEffect(CardType.LAND));
    }
}
