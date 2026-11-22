package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "PLC", collectorNumber = "71")
@CardRegistration(set = "MMA", collectorNumber = "84")
@CardRegistration(set = "TSR", collectorNumber = "114")
public class Extirpate extends Card {

    public Extirpate() {
        addSpellEffect(ExileCardsWithSameNameEffect.all(Subjects.targetCard(
                new CardNotPredicate(CardPredicateUtils.basicLand()), GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
