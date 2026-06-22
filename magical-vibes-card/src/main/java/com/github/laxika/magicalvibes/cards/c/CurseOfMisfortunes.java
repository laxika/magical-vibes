package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect;

@CardRegistration(set = "DKA", collectorNumber = "56")
public class CurseOfMisfortunes extends Card {

    public CurseOfMisfortunes() {
        // At the beginning of your upkeep, you may search your library for a Curse card that
        // doesn't have the same name as a Curse attached to enchanted player, put it onto the
        // battlefield attached to that player, then shuffle.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect(),
                "Search your library for a Curse card?"
        ));
    }
}
