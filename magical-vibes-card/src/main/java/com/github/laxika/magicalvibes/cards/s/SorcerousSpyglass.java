package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "248")
public class SorcerousSpyglass extends Card {

    public SorcerousSpyglass() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect(List.of(), true));
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect());
    }
}
