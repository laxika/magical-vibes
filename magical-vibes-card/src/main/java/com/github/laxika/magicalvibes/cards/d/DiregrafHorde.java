package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "MID", collectorNumber = "96")
public class DiregrafHorde extends Card {

    public DiregrafHorde() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenThenEffect(
                CreateTokenEffect.blackZombieWithDecayed(2),
                new ExileCardsFromGraveyardEffect(2, 0)));
    }
}
