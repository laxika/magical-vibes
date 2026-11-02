package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "31")
public class TempestHawk extends Card {

    public TempestHawk() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Tempest Hawk")),
                "Search your library for a card named Tempest Hawk?"
        ));
    }
}
