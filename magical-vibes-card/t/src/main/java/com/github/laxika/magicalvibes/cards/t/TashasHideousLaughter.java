package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AFR", collectorNumber = "78")
@CardRegistration(set = "SLD", collectorNumber = "1395")
public class TashasHideousLaughter extends Card {

    public TashasHideousLaughter() {
        addSpellEffect(ExileCardsFromTopUntilEffect.untilTotalManaValue(Subjects.eachOpponent(), 20));
    }
}
