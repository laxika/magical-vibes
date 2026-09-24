package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "HOC", collectorNumber = "9")
public class SmaugTheImpenetrable extends Card {

    public SmaugTheImpenetrable() {
        addEffect(EffectSlot.ON_NONCOMBAT_DAMAGE_TO_SELF,
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
