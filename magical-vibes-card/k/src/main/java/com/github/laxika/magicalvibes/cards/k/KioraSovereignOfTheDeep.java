package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.KioraSovereignOfTheDeepTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "35")
@CardRegistration(set = "MAT", collectorNumber = "85")
@CardRegistration(set = "MAT", collectorNumber = "135")
@CardRegistration(set = "MAT", collectorNumber = "170")
@CardRegistration(set = "MAT", collectorNumber = "214")
public class KioraSovereignOfTheDeep extends Card {

    public KioraSovereignOfTheDeep() {
        addTrigger(Triggers.youCastSpell(), new KioraSovereignOfTheDeepTriggerEffect());
    }
}
