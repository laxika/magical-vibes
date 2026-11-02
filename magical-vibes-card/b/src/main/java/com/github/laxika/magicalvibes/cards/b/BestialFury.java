package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "67a")
@CardRegistration(set = "ALL", collectorNumber = "67b")
@CardRegistration(set = "ME1", collectorNumber = "88")
public class BestialFury extends Card {

    public BestialFury() {
        // Enchant creature
        target(TargetFilters.creature());

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RegisterDrawCardsAtNextUpkeepEffect());

        // Whenever enchanted creature becomes blocked, it gets +4/+0 and gains trample until end of turn.
        addTrigger(Triggers.becomesBlocked(),
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(4, 0, Keyword.TRAMPLE));
    }
}
