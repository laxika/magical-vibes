package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;

@CardRegistration(set = "DSK", collectorNumber = "42")
public class AbhorrentOculus extends Card {

    public AbhorrentOculus() {
        addEffect(EffectSlot.SPELL, new ExileNCardsFromGraveyardCost(6, null));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, ManifestDreadEffect.forController());
    }
}
