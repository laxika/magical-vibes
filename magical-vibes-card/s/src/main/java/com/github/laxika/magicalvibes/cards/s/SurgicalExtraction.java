package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "NPH", collectorNumber = "74")
@CardRegistration(set = "MM2", collectorNumber = "99")
@CardRegistration(set = "SLD", collectorNumber = "2003")
@CardRegistration(set = "SLD", collectorNumber = "2022")
@CardRegistration(set = "2X2", collectorNumber = "94")
@CardRegistration(set = "OTP", collectorNumber = "19")
public class SurgicalExtraction extends Card {

    public SurgicalExtraction() {
        addSpellEffect(ExileCardsWithSameNameEffect.anyNumber(Subjects.targetCard(
                new CardNotPredicate(CardPredicateUtils.basicLand()), GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
