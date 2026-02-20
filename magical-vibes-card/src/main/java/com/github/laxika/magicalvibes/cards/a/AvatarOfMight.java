package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect;

@CardRegistration(set = "10E", collectorNumber = "251")
public class AvatarOfMight extends Card {

    public AvatarOfMight() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect(4, 6));
    }
}
