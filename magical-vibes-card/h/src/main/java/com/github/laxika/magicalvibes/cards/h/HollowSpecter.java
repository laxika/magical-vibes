package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaRevealCardsChooseOneToDiscardEffect;

@CardRegistration(set = "LGN", collectorNumber = "75")
public class HollowSpecter extends Card {

    public HollowSpecter() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PayXManaRevealCardsChooseOneToDiscardEffect());
    }
}
