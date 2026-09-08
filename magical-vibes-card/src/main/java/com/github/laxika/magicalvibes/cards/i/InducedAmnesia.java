package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;

@CardRegistration(set = "RIX", collectorNumber = "40")
public class InducedAmnesia extends Card {

    public InducedAmnesia() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect());
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
