package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCombatOpponentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "EMN", collectorNumber = "117")
@CardRegistration(set = "SIR", collectorNumber = "141")
public class AssembledAlphas extends Card {

    public AssembledAlphas() {
        addSimultaneousEffects(EffectSlot.ON_BLOCK,
                new DealDamageToCombatOpponentControllerEffect(new Fixed(3)),
                new DealDamageToTargetCreatureEffect(3));

        addSimultaneousEffects(EffectSlot.ON_BECOMES_BLOCKED, TriggerMode.PER_BLOCKER,
                new DealDamageToCombatOpponentControllerEffect(new Fixed(3)),
                new DealDamageToTargetCreatureEffect(3));
    }
}
