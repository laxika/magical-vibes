package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "239")
@CardRegistration(set = "5ED", collectorNumber = "287")
public class Crumble extends Card {

    public Crumble() {
        // Destroy target artifact. It can't be regenerated. That artifact's controller gains life equal to its mana value.
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.MANA_VALUE, new GainLifeEffect(new EventValue()), ThenEffectRecipient.TARGET_CONTROLLER, true));
    }
}
