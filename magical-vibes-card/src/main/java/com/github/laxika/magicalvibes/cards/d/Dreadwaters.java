package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "AVR", collectorNumber = "49")
@CardRegistration(set = "ORI", collectorNumber = "56")
public class Dreadwaters extends Card {

    public Dreadwaters() {
        // Target player mills X cards, where X is the number of lands you control.
        addEffect(EffectSlot.SPELL, new MillEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                MillRecipient.TARGET_PLAYER));
    }
}
