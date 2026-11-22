package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOM", collectorNumber = "69")
public class Memoricide extends Card {

    public Memoricide() {
        addSpellEffect(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND));
        addSpellEffect(ExileCardsWithSameNameEffect.anyNumberWithChosenName(Subjects.targetPlayer()));
    }
}
