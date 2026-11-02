package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellWhenCounteredOrFizzlesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "68")
public class TrialAndError extends Card {

    public TrialAndError() {
        addTrigger(Triggers.thisSpellCounteredOrFizzles(),
                new CopyThisSpellWhenCounteredOrFizzlesEffect());
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
    }
}
