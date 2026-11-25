package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EpicEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "119")
public class UndyingFlames extends Card {

    public UndyingFlames() {
        addSpellEffect(ExileCardsFromTopUntilEffect.untilNonland(Subjects.you()));
        addSpellEffect(DealDamageEffect.toAnyTarget(AmountThisWay.exiledManaValue()));
        addSpellEffect(new EpicEffect());
    }
}
