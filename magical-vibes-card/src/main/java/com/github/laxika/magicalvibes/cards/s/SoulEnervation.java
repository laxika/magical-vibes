package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "106")
public class SoulEnervation extends Card {

    public SoulEnervation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(-4, -4));
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD, new GainLifeEffect(1));
    }
}
