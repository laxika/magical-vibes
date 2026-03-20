package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;

@CardRegistration(set = "DOM", collectorNumber = "126")
public class GhituJourneymage extends Card {

    public GhituJourneymage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ControlsAnotherSubtypeConditionalEffect(CardSubtype.WIZARD,
                        new DealDamageToEachOpponentEffect(2)));
    }
}
