package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;

@CardRegistration(set = "MSC", collectorNumber = "91")
@CardRegistration(set = "MSC", collectorNumber = "412")
public class QuicksilverSpeedster extends Card {

    public QuicksilverSpeedster() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsTapped(), new GrantFlashToCardTypeEffect(null)));
    }
}
