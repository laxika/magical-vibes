package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "AER", collectorNumber = "75")
public class YahennisExpertise extends Card {

    public YahennisExpertise() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-3, -3));
        addEffect(EffectSlot.SPELL, new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                new CardMaxManaValuePredicate(3)));
    }
}
