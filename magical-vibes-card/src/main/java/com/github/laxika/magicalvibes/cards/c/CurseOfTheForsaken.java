package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingEnchantedPlayerPredicate;

@CardRegistration(set = "C13", collectorNumber = "8")
public class CurseOfTheForsaken extends Card {

    public CurseOfTheForsaken() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsAttackingEnchantedPlayerPredicate(),
                        new GainLifeEffect(new Fixed(1), GainLifeRecipient.TARGET_CONTROLLER)));
    }
}
