package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandCardExiledWithSourceMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;

@CardRegistration(set = "C21", collectorNumber = "35")
public class AuthorOfShadows extends Card {

    public AuthorOfShadows() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileGraveyardCardsEffect.allOpponentsWithSource());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseNonlandCardExiledWithSourceMayCastEffect());
    }
}
