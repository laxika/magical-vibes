package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "KHM", collectorNumber = "170")
@CardRegistration(set = "MUL", collectorNumber = "26")
@CardRegistration(set = "MUL", collectorNumber = "91")
@CardRegistration(set = "MUL", collectorNumber = "156")
@CardRegistration(set = "FCA", collectorNumber = "46")
public class FynnTheFangbearer extends Card {

    public FynnTheFangbearer() {
        // Whenever a deathtouch creature you control deals combat damage to a player,
        // that player gets two poison counters.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER)));
    }
}
