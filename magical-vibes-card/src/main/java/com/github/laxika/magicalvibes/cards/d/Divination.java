package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "49")
@CardRegistration(set = "DOM", collectorNumber = "52")
@CardRegistration(set = "DKA", collectorNumber = "35")
@CardRegistration(set = "BNG", collectorNumber = "36")
@CardRegistration(set = "M12", collectorNumber = "50")
@CardRegistration(set = "M13", collectorNumber = "47")
@CardRegistration(set = "M14", collectorNumber = "52")
@CardRegistration(set = "M15", collectorNumber = "52")
@CardRegistration(set = "M19", collectorNumber = "51")
public class Divination extends Card {

    public Divination() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
