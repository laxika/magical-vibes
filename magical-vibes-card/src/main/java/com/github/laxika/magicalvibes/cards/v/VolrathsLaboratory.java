package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOfChosenColorAndSubtypeEffect;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "142")
public class VolrathsLaboratory extends Card {

    public VolrathsLaboratory() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new CreateTokenOfChosenColorAndSubtypeEffect()),
                "{5}, {T}: Create a 2/2 creature token of the chosen color and type."
        ));
    }
}
