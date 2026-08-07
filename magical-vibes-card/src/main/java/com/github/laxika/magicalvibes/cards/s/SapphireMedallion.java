package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "TMP", collectorNumber = "306")
public class SapphireMedallion extends Card {

    public SapphireMedallion() {
        // Blue spells you cast cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.BLUE), 1, CostModificationScope.SELF));
    }
}
