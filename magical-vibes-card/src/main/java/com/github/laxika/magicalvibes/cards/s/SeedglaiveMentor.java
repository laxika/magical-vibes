package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;

@CardRegistration(set = "BLB", collectorNumber = "231")
public class SeedglaiveMentor extends Card {

    public SeedglaiveMentor() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new TriggeringSpellControllerConditionalEffect(new OncePerTurnTriggerEffect(
                        new PutCountersOnSourceEffect(1, 1, 1))));
    }
}
