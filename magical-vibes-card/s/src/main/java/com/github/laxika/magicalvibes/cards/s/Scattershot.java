package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "102")
public class Scattershot extends Card {

    public Scattershot() {
        addSpellEffect(DealDamageEffect.toTargetCreature(1));
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
