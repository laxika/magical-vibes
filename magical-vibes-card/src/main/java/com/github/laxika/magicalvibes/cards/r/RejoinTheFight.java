package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCardFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "FIC", collectorNumber = "49")
@CardRegistration(set = "FIC", collectorNumber = "118")
public class RejoinTheFight extends Card {

    public RejoinTheFight() {
        addEffect(EffectSlot.SPELL, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new EachOpponentChoosesCreatureCardFromControllerGraveyardToBattlefieldEffect());
    }
}
