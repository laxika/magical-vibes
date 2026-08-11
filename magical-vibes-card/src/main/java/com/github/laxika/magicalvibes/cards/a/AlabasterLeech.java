package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "INV", collectorNumber = "1")
public class AlabasterLeech extends Card {

    public AlabasterLeech() {
        // White spells you cast cost {W} more to cast (modeled as +1 generic).
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardColorPredicate(CardColor.WHITE), 1, CostModificationScope.SELF));
    }
}
