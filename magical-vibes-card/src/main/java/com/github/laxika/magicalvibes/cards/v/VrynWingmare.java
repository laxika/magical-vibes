package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "40")
public class VrynWingmare extends Card {

    public VrynWingmare() {
        // Noncreature spells cost {1} more to cast.
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), 1, CostModificationScope.ALL));
    }
}
