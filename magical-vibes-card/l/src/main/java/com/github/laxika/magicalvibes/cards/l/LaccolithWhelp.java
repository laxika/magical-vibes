package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "91")
public class LaccolithWhelp extends Card {

    public LaccolithWhelp() {
        addTrigger(Triggers.becomesBlocked(),
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(new SourcePower()),
                        new AssignNoCombatDamageEffect()),
                        "have it deal damage equal to its power to target creature?"));
    }
}
