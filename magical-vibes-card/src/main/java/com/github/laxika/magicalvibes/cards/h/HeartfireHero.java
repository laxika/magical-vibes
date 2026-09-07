package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;

@CardRegistration(set = "BLB", collectorNumber = "138")
public class HeartfireHero extends Card {

    public HeartfireHero() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new TriggeringSpellControllerConditionalEffect(new OncePerTurnTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
        addEffect(EffectSlot.ON_DEATH,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
    }
}
