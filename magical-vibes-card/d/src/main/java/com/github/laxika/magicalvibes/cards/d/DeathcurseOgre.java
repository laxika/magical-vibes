package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "109")
public class DeathcurseOgre extends Card {

    public DeathcurseOgre() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(3, LoseLifeRecipient.EACH_PLAYER));
    }
}
