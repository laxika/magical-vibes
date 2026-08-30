package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnFromGraveyardInsteadOfLearnEffect;

@CardRegistration(set = "STX", collectorNumber = "113")
public class RetrieverPhoenix extends Card {

    public RetrieverPhoenix() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(), new LearnEffect()));
        addEffect(EffectSlot.STATIC, new ReturnFromGraveyardInsteadOfLearnEffect());
    }
}
