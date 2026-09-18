package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExceptFirstDrawStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HOC", collectorNumber = "19")
@CardRegistration(set = "HOC", collectorNumber = "59")
public class OrcishBowmasters extends Card {

    public OrcishBowmasters() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmassGoblinsEffect(1, CardSubtype.ORC));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new ExceptFirstDrawStepTriggerEffect(SequenceEffect.of(
                        new DealDamageToAnyTargetEffect(1),
                        new AmassGoblinsEffect(1, CardSubtype.ORC))));
    }
}
