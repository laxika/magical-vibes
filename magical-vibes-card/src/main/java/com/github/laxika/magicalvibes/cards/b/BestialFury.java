package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALL", collectorNumber = "67a")
@CardRegistration(set = "ALL", collectorNumber = "67b")
public class BestialFury extends Card {

    public BestialFury() {
        // Enchant creature
        target(TargetFilters.creature());

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterDrawCardsAtNextUpkeepEffect());

        // Whenever enchanted creature becomes blocked, it gets +4/+0 and gains trample until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(4, 0, Keyword.TRAMPLE));
    }
}
