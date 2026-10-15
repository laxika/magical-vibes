package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "105")
public class Squeeze extends Card {

    public Squeeze() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTypePredicate(CardType.SORCERY), 3, CostModificationScope.ALL));
    }
}
