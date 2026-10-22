package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MBS", collectorNumber = "57")
public class VirulentWound extends Card {

    public VirulentWound() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE))
                .addEffect(EffectSlot.SPELL, new ResolveEffectOnTargetDeathThisTurnEffect(
                        new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)));
    }
}
