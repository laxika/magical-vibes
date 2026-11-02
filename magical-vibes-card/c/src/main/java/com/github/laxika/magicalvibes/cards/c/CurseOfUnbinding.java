package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "12")
@CardRegistration(set = "MIC", collectorNumber = "50")
public class CurseOfUnbinding extends Card {

    public CurseOfUnbinding() {
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect.forEnchantedPlayer());
    }
}
