package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CHK", collectorNumber = "105")
public class CranialExtraction extends Card {

    public CranialExtraction() {
        addSpellEffect(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND));
        addSpellEffect(ExileCardsWithSameNameEffect.allWithChosenName(Subjects.targetPlayer()));
    }
}
