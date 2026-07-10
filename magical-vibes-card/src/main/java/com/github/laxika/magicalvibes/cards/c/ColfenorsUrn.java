package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

@CardRegistration(set = "LRW", collectorNumber = "254")
public class ColfenorsUrn extends Card {

    public ColfenorsUrn() {
        // Whenever a creature with toughness 4 or greater is put into your graveyard from the
        // battlefield, you may exile it (tracked with this artifact).
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentToughnessAtLeastPredicate(4),
                        new MayEffect(new ExileTriggeringCreatureAndTrackWithSourceEffect(),
                                "Exile the creature with Colfenor's Urn?")));
        // At the beginning of the end step, if three or more cards have been exiled with this
        // artifact, sacrifice it. If you do, return those cards to the battlefield under their
        // owner's control.
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new SacrificeSelfAndReturnCardsExiledWithSourceEffect(3));
    }
}
