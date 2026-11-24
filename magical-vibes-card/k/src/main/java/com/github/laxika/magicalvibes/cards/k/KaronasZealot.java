package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "18")
@CardRegistration(set = "A25", collectorNumber = "18")
public class KaronasZealot extends Card {

    public KaronasZealot() {
        addMorph("{3}{W}{W}");
        target(TargetFilters.creature()).addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                RedirectNextDamageEffect.allThisTurn(RedirectRole.SOURCE_PERMANENT, RedirectRole.TARGET,
                        TargetPredicates.creature()));
    }
}
