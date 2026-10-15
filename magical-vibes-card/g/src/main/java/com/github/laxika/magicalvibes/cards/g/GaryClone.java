package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "16")
@CardRegistration(set = "PIP", collectorNumber = "544")
public class GaryClone extends Card {

    public GaryClone() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{2}")));
        addEffect(EffectSlot.ON_ATTACK,
                new BoostAllOwnCreaturesEffect(1, 0, new PermanentNamedPredicate("Gary Clone")));
    }
}
