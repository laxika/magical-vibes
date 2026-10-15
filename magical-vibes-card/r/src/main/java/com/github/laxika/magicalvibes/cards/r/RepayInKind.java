package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LowestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;

@CardRegistration(set = "ROE", collectorNumber = "125")
public class RepayInKind extends Card {

    public RepayInKind() {
        addEffect(EffectSlot.SPELL, new SetLifeTotalEffect(
                new LowestLifeTotalAmongPlayers(), SetLifeTotalRecipient.EACH_PLAYER));
    }
}
