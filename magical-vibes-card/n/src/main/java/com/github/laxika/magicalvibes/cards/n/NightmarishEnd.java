package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "JOU", collectorNumber = "76")
public class NightmarishEnd extends Card {

    public NightmarishEnd() {
        var minusX = new Scaled(new CardsInHand(CountScope.CONTROLLER), -1);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(minusX, minusX));
    }
}
