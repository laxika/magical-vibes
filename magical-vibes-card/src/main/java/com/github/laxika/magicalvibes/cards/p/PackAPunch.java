package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Pack a Punch, the prepare spell of Kirol, History Buff // Pack a Punch (SOS 198). */
public class PackAPunch extends Card {

    public PackAPunch() {
        addEffect(EffectSlot.SPELL, new MillEffect(1, MillRecipient.CONTROLLER));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
