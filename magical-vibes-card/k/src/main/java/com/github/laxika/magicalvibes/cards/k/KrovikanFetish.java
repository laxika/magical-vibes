package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "172")
@CardRegistration(set = "ICE", collectorNumber = "140")
@CardRegistration(set = "ME2", collectorNumber = "100")
public class KrovikanFetish extends Card {

    public KrovikanFetish() {
        // Enchant creature — enchanted creature gets +1/+1.
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
