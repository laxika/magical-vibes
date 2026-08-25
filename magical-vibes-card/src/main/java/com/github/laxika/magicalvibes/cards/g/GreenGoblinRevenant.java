package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SPM", collectorNumber = "130")
public class GreenGoblinRevenant extends Card {

    public GreenGoblinRevenant() {
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                new DrawCardEffect(new CardsDiscardedOrCycledThisTurn())
        ));
    }
}
