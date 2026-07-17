package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesDevoured;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ALA", collectorNumber = "148")
public class Skullmulcher extends Card {

    public Skullmulcher() {
        // Devour 1 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        // When this creature enters, draw a card for each creature it devoured.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(new CreaturesDevoured()));
    }
}
