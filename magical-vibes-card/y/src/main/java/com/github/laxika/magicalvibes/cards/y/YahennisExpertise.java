package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "AER", collectorNumber = "75")
@CardRegistration(set = "KLR", collectorNumber = "112")
public class YahennisExpertise extends Card {

    public YahennisExpertise() {
        addSpellEffect(new BoostAllCreaturesEffect(-3, -3));
        addSpellEffect(new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                new CardMaxManaValuePredicate(3)));
    }
}
