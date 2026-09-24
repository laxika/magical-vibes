package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YEOE", collectorNumber = "26")
public class ValMaroonedSurveyor extends Card {

    public ValMaroonedSurveyor() {
        CardEffect trigger = SequenceEffect.of(
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(2));

        addEffect(EffectSlot.ON_CONTROLLER_DISCOVERS, trigger);
        addEffect(EffectSlot.ON_CONTROLLER_INVESTIGATES_EACH_TIME, trigger);
        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, trigger);
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS, trigger);
        addEffect(EffectSlot.ON_CONTROLLER_SEEKS, trigger);
    }
}
