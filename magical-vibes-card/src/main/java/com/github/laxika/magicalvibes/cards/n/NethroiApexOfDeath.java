package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect;

@CardRegistration(set = "IKO", collectorNumber = "197")
public class NethroiApexOfDeath extends Card {

    public NethroiApexOfDeath() {
        addEffect(EffectSlot.ON_SELF_MUTATES,
                new ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(10));
    }
}
