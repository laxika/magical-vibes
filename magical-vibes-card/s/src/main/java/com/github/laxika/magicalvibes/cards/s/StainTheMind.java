package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M15", collectorNumber = "117")
@CardRegistration(set = "PIO", collectorNumber = "339")
public class StainTheMind extends Card {

    public StainTheMind() {
        addSpellEffect(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND));
        addSpellEffect(ExileCardsWithSameNameEffect.anyNumberWithChosenName(Subjects.targetPlayer()));
    }
}
