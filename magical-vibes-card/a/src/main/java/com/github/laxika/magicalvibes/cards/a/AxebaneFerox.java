package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessCollectsEvidenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "153")
@CardRegistration(set = "MKM", collectorNumber = "409")
@CardRegistration(set = "MKM", collectorNumber = "428")
public class AxebaneFerox extends Card {

    public AxebaneFerox() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessCollectsEvidenceEffect(4));
    }
}
