package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;

@CardRegistration(set = "FUT", collectorNumber = "1")
public class AngelOfSalvation extends Card {

    public AngelOfSalvation() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                PreventDividedDamageEffect.chosenAmongAnyTargetsEtb(5));
    }
}
