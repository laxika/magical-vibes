package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;

@CardRegistration(set = "USG", collectorNumber = "149")
public class PlanarVoid extends Card {

    public PlanarVoid() {
        addEffect(EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ExileTriggeringCardFromGraveyardEffect());
        addEffect(EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new ExileTriggeringCardFromGraveyardEffect());
    }
}
