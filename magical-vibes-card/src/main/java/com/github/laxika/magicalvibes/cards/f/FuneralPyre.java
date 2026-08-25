package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardCreateTokenForOwnerEffect;

@CardRegistration(set = "JUD", collectorNumber = "10")
public class FuneralPyre extends Card {

    public FuneralPyre() {
        addEffect(EffectSlot.SPELL, new ExileTargetCardFromGraveyardCreateTokenForOwnerEffect(
                CreateTokenEffect.whiteSpirit(1)));
    }
}
