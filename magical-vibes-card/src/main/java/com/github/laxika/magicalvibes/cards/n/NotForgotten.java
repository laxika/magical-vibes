package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "SOI", collectorNumber = "30")
public class NotForgotten extends Card {

    public NotForgotten() {
        addEffect(EffectSlot.SPELL, new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect());
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(1));
    }
}
