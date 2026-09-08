package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "DSK", collectorNumber = "142")
public class IrreverentGremlin extends Card {

    public IrreverentGremlin() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentPowerAtMostPredicate(2),
                        new OncePerTurnTriggerEffect(
                                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"))));
    }
}
