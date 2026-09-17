package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C13", collectorNumber = "203")
public class OloroAgelessAscetic extends Card {

    public OloroAgelessAscetic() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(2));
        addEffect(EffectSlot.COMMAND_ZONE_UPKEEP_TRIGGERED, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new MayPayManaEffect(
                "{1}",
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                "Pay {1} to draw a card and have each opponent lose 1 life?"));
    }
}
