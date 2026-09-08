package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOI", collectorNumber = "63")
public class ForgottenCreation extends Card {

    public ForgottenCreation() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new DiscardOwnHandThenDrawThatManyEffect(),
                "Discard your hand and draw that many cards?"));
    }
}
