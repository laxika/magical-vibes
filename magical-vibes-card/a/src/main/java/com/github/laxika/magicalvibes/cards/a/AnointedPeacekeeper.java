package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenNamePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "2")
public class AnointedPeacekeeper extends Card {

    public AnointedPeacekeeper() {
        addStaticEffect(new ChooseCardNameOnEnterEffect(
                List.of(), ChooseCardNameOnEnterEffect.HandAccess.LOOK_AT_OPPONENT_HAND));
        addStaticEffect(new IncreaseSpellCostEffect(
                new CardHasSourceChosenNamePredicate(), 2, CostModificationScope.OPPONENT));
        addStaticEffect(new IncreaseActivatedAbilityCostEffect(
                new PermanentHasSourceChosenNamePredicate(), 2, false, true));
    }
}
