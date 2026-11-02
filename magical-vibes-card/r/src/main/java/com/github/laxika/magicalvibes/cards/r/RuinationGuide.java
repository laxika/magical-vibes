package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "64")
public class RuinationGuide extends Card {

    public RuinationGuide() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES, new PermanentIsColorlessPredicate()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardsOfDefendingPlayerLibraryEffect(1));
    }
}
