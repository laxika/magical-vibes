package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C16", collectorNumber = "15")
public class PartingThoughts extends Card {

    public PartingThoughts() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentThenEffect(
                        EventStat.TOTAL_COUNTERS,
                        SequenceEffect.of(
                                new DrawCardEffect(new EventValue()),
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER)),
                        ThenEffectRecipient.CONTROLLER));
    }
}
