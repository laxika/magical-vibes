package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "CON", collectorNumber = "49")
public class Nyxathid extends Card {

    public Nyxathid() {
        // As this creature enters, choose an opponent.
        // This creature gets -1/-1 for each card in the chosen player's hand.
        // The chosen opponent is modelled implicitly as the controller's opponent(s)
        // (Cursed Rack precedent), so the debuff scales with opponents' hand size.
        Scaled minusOnePerCard = new Scaled(new CardsInHand(CountScope.OPPONENTS), -1);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(minusOnePerCard, minusOnePerCard));
    }
}
