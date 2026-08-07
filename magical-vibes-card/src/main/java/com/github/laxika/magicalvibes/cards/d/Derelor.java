package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "6ED", collectorNumber = "120")
@CardRegistration(set = "5ED", collectorNumber = "155")
public class Derelor extends Card {

    public Derelor() {
        // Black spells you cast cost {B} more to cast (modeled as +1 generic).
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardColorPredicate(CardColor.BLACK), 1, CostModificationScope.SELF));
    }
}
