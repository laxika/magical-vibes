package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;

@CardRegistration(set = "SOS", collectorNumber = "233")
public class StartledRelicSloth extends Card {

    public StartledRelicSloth() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard());
    }
}
