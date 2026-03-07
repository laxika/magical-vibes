package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;

@CardRegistration(set = "NPH", collectorNumber = "37")
public class JinGitaxiasCoreAugur extends Card {

    public JinGitaxiasCoreAugur() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DrawCardEffect(7));
        addEffect(EffectSlot.STATIC, new ReduceOpponentMaxHandSizeEffect(7));
    }
}
