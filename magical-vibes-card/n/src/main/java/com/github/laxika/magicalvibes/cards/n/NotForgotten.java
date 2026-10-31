package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "SOI", collectorNumber = "30")
public class NotForgotten extends Card {

    public NotForgotten() {
        addSpellEffect(new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect());
        addSpellEffect(CreateTokenEffect.whiteSpirit(1));
    }
}
