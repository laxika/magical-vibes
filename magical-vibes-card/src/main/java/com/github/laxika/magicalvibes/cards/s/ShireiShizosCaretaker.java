package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "BOK", collectorNumber = "81")
public class ShireiShizosCaretaker extends Card {

    public ShireiShizosCaretaker() {
        // "Whenever a creature with power 1 or less is put into your graveyard from the battlefield,
        // you may return that card to the battlefield at the beginning of the next end step if
        // Shirei is still on the battlefield."
        // The power check reads the dying permanent's last-known power, so a creature shrunk to 1 or
        // less before it died qualifies. The delayed return is gated on this exact Shirei permanent
        // still being on the battlefield at the end step.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentPowerAtMostPredicate(1),
                        new MayEffect(
                                new RegisterDelayedReturnDyingCreatureUnderControlEffect(true),
                                "return that creature to the battlefield at the beginning of the next end step?")));
    }
}
