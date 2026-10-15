package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardSharingCardTypeWithDiscardedCardsEffect;

@CardRegistration(set = "YSNC", collectorNumber = "22")
public class DivinerOfFates extends Card {

    public DivinerOfFates() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawDiscardAndConniveEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new OncePerTurnTriggerEffect(new SeekCardSharingCardTypeWithDiscardedCardsEffect()));
    }
}
