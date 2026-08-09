package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "75")
public class YawgmothsBargain extends Card {

    public YawgmothsBargain() {
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new DrawCardEffect(1)),
                "Pay 1 life: Draw a card."));
    }
}
