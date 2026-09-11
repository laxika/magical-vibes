package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NEO", collectorNumber = "243")
public class ContainmentConstruct extends Card {

    public ContainmentConstruct() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new MayEffect(
                ExileDiscardedCardFromGraveyardEffect.withPlayPermission(),
                "Exile that card from your graveyard?"));
    }
}
