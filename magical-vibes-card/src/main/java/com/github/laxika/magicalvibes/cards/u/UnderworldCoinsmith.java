package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "157")
public class UnderworldCoinsmith extends Card {

    public UnderworldCoinsmith() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(false, "{W}{B}",
                List.of(new PayLifeCost(1), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                "{W}{B}, Pay 1 life: Each opponent loses 1 life."));
    }
}
