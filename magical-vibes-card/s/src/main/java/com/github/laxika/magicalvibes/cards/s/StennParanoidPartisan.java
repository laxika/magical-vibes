package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "221")
public class StennParanoidPartisan extends Card {

    public StennParanoidPartisan() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardTypeOnEnterEffect(
                List.of(CardType.CREATURE, CardType.LAND)));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardHasSourceChosenCardTypePredicate(), 1, CostModificationScope.SELF));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{U}",
                List.of(FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false)),
                "{1}{W}{U}: Exile Stenn. Return it to the battlefield under its owner's control at the beginning of the next end step."
        ));
    }
}
