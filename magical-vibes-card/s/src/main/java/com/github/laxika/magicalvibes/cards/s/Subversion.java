package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ULG", collectorNumber = "68")
@CardRegistration(set = "BRB", collectorNumber = "82")
public class Subversion extends Card {

    public Subversion() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
