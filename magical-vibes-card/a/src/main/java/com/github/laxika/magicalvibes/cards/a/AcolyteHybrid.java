package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "70")
public class AcolyteHybrid extends Card {

    public AcolyteHybrid() {
        // Whenever this creature attacks, destroy up to one target artifact. If an artifact is
        // destroyed this way, its controller draws a card.
        target(TargetFilters.artifact(), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new DestroyTargetPermanentThenEffect(
                                EventStat.NONE,
                                new DrawCardEffect(),
                                ThenEffectRecipient.TARGET_CONTROLLER,
                                null,
                                false,
                                true));
    }
}
