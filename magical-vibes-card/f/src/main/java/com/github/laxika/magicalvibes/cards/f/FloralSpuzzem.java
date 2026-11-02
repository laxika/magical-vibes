package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "187")
public class FloralSpuzzem extends Card {

    public FloralSpuzzem() {
        // Whenever this creature attacks and isn't blocked, you may destroy target artifact
        // defending player controls. If you do, this creature assigns no combat damage this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(
                        new DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect(
                                new PermanentIsArtifactPredicate(), "artifact"),
                        "You may destroy target artifact defending player controls. If you do, this creature assigns no combat damage this turn."));
    }
}
