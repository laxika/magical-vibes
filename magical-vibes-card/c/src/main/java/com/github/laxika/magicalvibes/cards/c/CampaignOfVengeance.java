package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "182")
@CardRegistration(set = "MB1", collectorNumber = "182")
public class CampaignOfVengeance extends Card {

    public CampaignOfVengeance() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new LoseLifeEffect(1, Subjects.defendingPlayer()),
                new GainLifeEffect(1));
    }
}
