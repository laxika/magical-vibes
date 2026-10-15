package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect;

@CardRegistration(set = "MIC", collectorNumber = "12")
@CardRegistration(set = "MIC", collectorNumber = "50")
public class CurseOfUnbinding extends Card {

    public CurseOfUnbinding() {
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect.forEnchantedPlayer());
    }
}
