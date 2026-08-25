package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KyrenFlamewright;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MOM", collectorNumber = "147")
public class InvasionOfMercadia extends Card {

    public InvasionOfMercadia() {
        setBackFaceCard(new KyrenFlamewright());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new DiscardAndDrawCardEffect(1, 2),
                        "Discard a card to draw two cards?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "KyrenFlamewright";
    }
}
