package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;

@CardRegistration(set = "WTH", collectorNumber = "101")
public class Firestorm extends Card {

    public Firestorm() {
        // Additional cost: discard X cards (the same X the spell is cast for).
        addCastingCost(new DiscardXCardsCost());
        // Firestorm deals X damage to each of X targets: one X-scaled any-target group,
        // each chosen target taking the full X (not divided).
        targetExactlyX(null, 1000).addSpellEffect(new DealDamageToEachTargetEffect(new XValue()));
    }
}
