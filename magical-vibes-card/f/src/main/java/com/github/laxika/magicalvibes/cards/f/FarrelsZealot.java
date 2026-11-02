package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FEM", collectorNumber = "3a")
@CardRegistration(set = "FEM", collectorNumber = "3b")
@CardRegistration(set = "FEM", collectorNumber = "3c")
@CardRegistration(set = "FEM", collectorNumber = "141")
@CardRegistration(set = "ME2", collectorNumber = "14")
public class FarrelsZealot extends Card {

    public FarrelsZealot() {
        // Whenever this creature attacks and isn't blocked, you may have it deal 3 damage to
        // target creature. If you do, this creature assigns no combat damage this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(3),
                        new AssignNoCombatDamageEffect()),
                        "have it deal 3 damage to target creature?"));
    }
}
