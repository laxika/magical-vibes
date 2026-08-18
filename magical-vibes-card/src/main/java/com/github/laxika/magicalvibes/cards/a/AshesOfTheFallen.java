package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnGraveyardCreatureCardsEffect;

@CardRegistration(set = "SOK", collectorNumber = "152")
public class AshesOfTheFallen extends Card {

    public AshesOfTheFallen() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantChosenSubtypeToOwnGraveyardCreatureCardsEffect());
    }
}
