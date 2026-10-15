package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "TDM", collectorNumber = "211")
public class NewWayForward extends Card {

    public NewWayForward() {
        addEffect(EffectSlot.SPELL,
                PreventDamageFromChosenSourceEffect.nextDamageToYouAndDamageSourceControllerAndDrawCards());
    }
}
