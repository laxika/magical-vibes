package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCombatOpponentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "EMN", collectorNumber = "117")
public class AssembledAlphas extends Card {

    public AssembledAlphas() {
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToCombatOpponentControllerEffect(new Fixed(3)));
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DealDamageToCombatOpponentControllerEffect(new Fixed(3)), TriggerMode.PER_BLOCKER);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DealDamageToTargetCreatureEffect(3), TriggerMode.PER_BLOCKER);
    }
}
