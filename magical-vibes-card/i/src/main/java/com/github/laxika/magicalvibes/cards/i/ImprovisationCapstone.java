package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.CastCardsWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SOS", collectorNumber = "120")
@CardRegistration(set = "SOS", collectorNumber = "294")
public class ImprovisationCapstone extends Card {

    public ImprovisationCapstone() {
        addSpellEffect(ExileCardsFromTopUntilEffect.untilTotalManaValue(Subjects.you(), 4));
        addSpellEffect(CastCardsWithoutPayingManaCostEffect.anyNumber(Subjects.cardsThisWay(StepKind.EXILE)));
    }
}
