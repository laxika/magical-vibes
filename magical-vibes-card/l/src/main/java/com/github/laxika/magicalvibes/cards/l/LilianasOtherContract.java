package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndReturnTransformedEffect;

@CardRegistration(set = "MB2", collectorNumber = "548")
public class LilianasOtherContract extends Card {

    public LilianasOtherContract() {
        setBackFaceCard(new LilianasUndeadMinion());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3));
        addEffect(EffectSlot.STATIC, new ReplaceControllerLossWithExileAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "LilianasUndeadMinion";
    }
}
