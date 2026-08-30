package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "137")
public class AnuridBrushhopper extends Card {

    public AnuridBrushhopper() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false)
                ),
                "Discard two cards: Exile Anurid Brushhopper. Return it to the battlefield under its owner's control at the beginning of the next end step."
        ));
    }
}
