package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "ELD", collectorNumber = "208")
public class DeathlessKnight extends Card {

    public DeathlessKnight() {
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE,
                new OncePerTurnTriggerEffect(new ReturnSourceCardFromGraveyardToOwnerHandEffect()));
    }
}
