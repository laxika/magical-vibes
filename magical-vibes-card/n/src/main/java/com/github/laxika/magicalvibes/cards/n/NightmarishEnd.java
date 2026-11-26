package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;

@CardRegistration(set = "JOU", collectorNumber = "76")
public class NightmarishEnd extends Card {

    public NightmarishEnd() {
        var minusX = new Scaled(new CardsInHand(CountScope.CONTROLLER), -1);
        addSpellEffect(BoostEffect.target(minusX, minusX));
    }
}
