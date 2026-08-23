package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TSP", collectorNumber = "66")
public class LooterIlKor extends Card {

    public LooterIlKor() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new DrawCardEffect(),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
