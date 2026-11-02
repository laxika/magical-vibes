package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "87")
public class LaccolithGrunt extends Card {

    public LaccolithGrunt() {
        // Whenever this creature becomes blocked, you may have it deal damage equal to its power
        // to target creature. If you do, this creature assigns no combat damage this turn.
        addTrigger(Triggers.becomesBlocked(),
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(new SourcePower()),
                        new AssignNoCombatDamageEffect()),
                        "have it deal damage equal to its power to target creature?"));
    }
}
