package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "CON", collectorNumber = "47")
@CardRegistration(set = "M19", collectorNumber = "101")
public class InfectiousHorror extends Card {

    public InfectiousHorror() {
        addEffect(EffectSlot.ON_ATTACK, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
    }
}
