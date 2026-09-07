package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TrugaCliffcharger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MOM", collectorNumber = "233")
public class InvasionOfErgamon extends Card {

    public InvasionOfErgamon() {
        setBackFaceCard(new TrugaCliffcharger());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TrugaCliffcharger";
    }
}
