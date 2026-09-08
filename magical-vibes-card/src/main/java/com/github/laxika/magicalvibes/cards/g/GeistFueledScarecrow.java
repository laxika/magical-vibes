package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "196")
public class GeistFueledScarecrow extends Card {

    public GeistFueledScarecrow() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTypePredicate(CardType.CREATURE), 1, CostModificationScope.SELF));
    }
}
