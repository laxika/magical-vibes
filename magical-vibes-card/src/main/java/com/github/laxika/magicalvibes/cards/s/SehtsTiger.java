package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceToControllerUntilEndOfTurnEffect;

@CardRegistration(set = "FUT", collectorNumber = "31")
public class SehtsTiger extends Card {

    public SehtsTiger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantProtectionChoiceToControllerUntilEndOfTurnEffect());
    }
}
