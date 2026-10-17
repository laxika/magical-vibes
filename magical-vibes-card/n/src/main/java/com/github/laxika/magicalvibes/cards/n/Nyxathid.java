package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;

@CardRegistration(set = "CON", collectorNumber = "49")
public class Nyxathid extends Card {

    public Nyxathid() {
        // As this creature enters, choose an opponent.
        // This creature gets -1/-1 for each card in the chosen player's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentOnEnterEffect());
        Scaled minusOnePerCard = new Scaled(new CardsInHand(CountScope.CHOSEN_PLAYER), -1);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(minusOnePerCard, minusOnePerCard));
    }
}
