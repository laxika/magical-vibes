package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "68")
public class TrialAndError extends Card {

    public TrialAndError() {
        addTrigger(Triggers.thisSpellCounteredOrFizzles(),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy Trial and Error?"));
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
    }
}
