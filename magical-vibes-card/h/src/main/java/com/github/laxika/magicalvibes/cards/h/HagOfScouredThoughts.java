package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HBG", collectorNumber = "H23")
public class HagOfScouredThoughts extends Card {

    public HagOfScouredThoughts() {
        // At the beginning of your upkeep, each opponent mills five cards. Then exile each opponent's graveyard.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new MillEffect(5, MillRecipient.EACH_OPPONENT),
                new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS)));
    }
}
