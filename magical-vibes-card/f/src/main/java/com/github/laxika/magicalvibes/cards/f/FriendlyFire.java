package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "101")
public class FriendlyFire extends Card {

    public FriendlyFire() {
        target(TargetFilters.creature())
                .addSpellEffect(new RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(new EventValue()))
                .addSpellEffect(new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
