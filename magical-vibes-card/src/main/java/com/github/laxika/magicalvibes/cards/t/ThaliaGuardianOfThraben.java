package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "24")
public class ThaliaGuardianOfThraben extends Card {

    public ThaliaGuardianOfThraben() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), 1));
    }
}
