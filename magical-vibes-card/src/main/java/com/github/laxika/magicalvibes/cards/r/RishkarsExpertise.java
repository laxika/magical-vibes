package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "AER", collectorNumber = "123")
public class RishkarsExpertise extends Card {

    public RishkarsExpertise() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new GreatestPowerAmongControlled()));
        addEffect(EffectSlot.SPELL, new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                new CardMaxManaValuePredicate(5)));
    }
}
