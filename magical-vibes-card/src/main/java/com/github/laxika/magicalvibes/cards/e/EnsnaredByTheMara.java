package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnsnaredByTheMaraVillainousChoiceEffect;

@CardRegistration(set = "WHO", collectorNumber = "84")
@CardRegistration(set = "WHO", collectorNumber = "384")
@CardRegistration(set = "WHO", collectorNumber = "689")
@CardRegistration(set = "WHO", collectorNumber = "975")
public class EnsnaredByTheMara extends Card {

    public EnsnaredByTheMara() {
        addEffect(EffectSlot.SPELL, new EnsnaredByTheMaraVillainousChoiceEffect());
    }
}
