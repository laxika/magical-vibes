package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;

@CardRegistration(set = "CSP", collectorNumber = "67")
public class PhyrexianEtchings extends Card {

    public PhyrexianEtchings() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{B}"));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DrawCardEffect(new CountersOnSource(CounterType.AGE)));
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new LoseLifeEffect(new Scaled(new CountersOnSource(CounterType.AGE), 2),
                        LoseLifeRecipient.CONTROLLER));
    }
}
