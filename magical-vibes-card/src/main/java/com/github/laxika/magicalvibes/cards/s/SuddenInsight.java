package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "AFR", collectorNumber = "77")
public class SuddenInsight extends Card {

    public SuddenInsight() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new DistinctManaValuesAmongCardsInGraveyard(CountScope.CONTROLLER, true)));
    }
}
