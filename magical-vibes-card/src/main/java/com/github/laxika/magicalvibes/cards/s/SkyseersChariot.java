package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "28")
public class SkyseersChariot extends Card {

    public SkyseersChariot() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardNameOnEnterEffect(List.of(CardType.LAND)));
        addEffect(EffectSlot.STATIC, new IncreaseActivatedAbilityCostEffect(
                new PermanentHasSourceChosenNamePredicate(), 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
