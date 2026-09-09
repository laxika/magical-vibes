package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "64")
public class RuinationGuide extends Card {

    public RuinationGuide() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES, new PermanentIsColorlessPredicate()));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsOfDefendingPlayerLibraryEffect(1));
    }
}
