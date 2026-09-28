package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "52")
@CardRegistration(set = "PIP", collectorNumber = "911")
@CardRegistration(set = "PIP", collectorNumber = "383")
@CardRegistration(set = "PIP", collectorNumber = "580")
public class WastelandRaider extends Card {

    public WastelandRaider() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerSacrificesCreatureEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{2}")));
    }
}
