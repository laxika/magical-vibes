package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "EMN", collectorNumber = "182")
public class CampaignOfVengeance extends Card {

    public CampaignOfVengeance() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new GainLifeEffect(1));
    }
}
