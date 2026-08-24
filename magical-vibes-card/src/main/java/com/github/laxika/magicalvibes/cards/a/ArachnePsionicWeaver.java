package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "2")
public class ArachnePsionicWeaver extends Card {

    public ArachnePsionicWeaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardTypeOnEnterEffect(List.of(CardType.CREATURE), true));
        addEffect(EffectSlot.STATIC,
                new IncreaseSpellCostEffect(new CardHasSourceChosenCardTypePredicate(), 1,
                        CostModificationScope.ALL));
    }
}
