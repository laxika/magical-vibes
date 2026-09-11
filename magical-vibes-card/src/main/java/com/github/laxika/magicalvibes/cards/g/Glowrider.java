package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "15")
public class Glowrider extends Card {

    public Glowrider() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), 1, CostModificationScope.ALL));
    }
}
