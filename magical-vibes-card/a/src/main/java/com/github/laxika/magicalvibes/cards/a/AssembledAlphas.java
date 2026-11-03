package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCombatOpponentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "117")
@CardRegistration(set = "SIR", collectorNumber = "141")
public class AssembledAlphas extends Card {

    public AssembledAlphas() {
        addSimultaneousTrigger(Triggers.blocks(),
                new DealDamageToCombatOpponentControllerEffect(new Fixed(3)),
                new DealDamageToTargetCreatureEffect(3));

        addSimultaneousTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new DealDamageToCombatOpponentControllerEffect(new Fixed(3)),
                new DealDamageToTargetCreatureEffect(3));
    }
}
