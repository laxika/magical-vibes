package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "59")
@CardRegistration(set = "ROE", collectorNumber = "102")
public class ContaminatedGround extends Card {

    public ContaminatedGround() {
        target(TargetFilters.land()).addStaticEffect(new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP));
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
