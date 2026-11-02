package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "105")
@CardRegistration(set = "ATH", collectorNumber = "41")
public class GoblinVandal extends Card {

    public GoblinVandal() {
        // Whenever this creature attacks and isn't blocked, you may pay {R}. If you do, destroy
        // target artifact defending player controls and this creature assigns no combat damage
        // this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayPayManaEffect("{R}",
                        new DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect(
                                new PermanentIsArtifactPredicate(), "artifact"),
                        "pay {R} to destroy target artifact defending player controls"));
    }
}
