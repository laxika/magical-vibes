package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "74a")
@CardRegistration(set = "ALL", collectorNumber = "74b")
@CardRegistration(set = "10E", collectorNumber = "211")
@CardRegistration(set = "9ED", collectorNumber = "196")
@CardRegistration(set = "8ED", collectorNumber = "192")
@CardRegistration(set = "DKM", collectorNumber = "13a")
@CardRegistration(set = "DKM", collectorNumber = "13b")
public class GuerrillaTactics extends Card {

    public GuerrillaTactics() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new DealDamageToAnyTargetEffect(4));
    }
}
