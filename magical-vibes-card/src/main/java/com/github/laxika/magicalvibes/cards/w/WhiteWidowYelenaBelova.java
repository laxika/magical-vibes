package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "MSC", collectorNumber = "676")
public class WhiteWidowYelenaBelova extends Card {

    public WhiteWidowYelenaBelova() {
        // Whenever a creature you control with deathtouch deals combat damage to a player,
        // put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        true));
    }
}
