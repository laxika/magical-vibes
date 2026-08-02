package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "CHK", collectorNumber = "109")
public class DeathcurseOgre extends Card {

    public DeathcurseOgre() {
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(3, LoseLifeRecipient.EACH_PLAYER));
    }
}
