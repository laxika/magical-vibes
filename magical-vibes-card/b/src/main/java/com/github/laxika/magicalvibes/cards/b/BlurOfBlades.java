package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOU", collectorNumber = "84")
@CardRegistration(set = "AKR", collectorNumber = "141")
public class BlurOfBlades extends Card {

    public BlurOfBlades() {
        target(TargetFilters.creature());
        addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
        addSpellEffect(new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
