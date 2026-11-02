package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "102")
public class Scattershot extends Card {

    public Scattershot() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(1));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
