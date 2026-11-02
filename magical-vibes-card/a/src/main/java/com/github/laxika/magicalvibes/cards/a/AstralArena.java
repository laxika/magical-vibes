package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "11")
@CardRegistration(set = "OPCA", collectorNumber = "14")
public class AstralArena extends Card {

    public AstralArena() {
        addStaticEffect(new MaximumCombatCreaturesEffect(1, 1));
        addTrigger(Triggers.chaosEnsues(), new MassDamageEffect(2));
    }
}
