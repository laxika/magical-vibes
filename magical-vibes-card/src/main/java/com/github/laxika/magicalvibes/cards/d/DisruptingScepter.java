package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "295")
@CardRegistration(set = "6ED", collectorNumber = "281")
@CardRegistration(set = "8ED", collectorNumber = "298")
@CardRegistration(set = "7ED", collectorNumber = "293")
@CardRegistration(set = "5ED", collectorNumber = "365")
@CardRegistration(set = "4ED", collectorNumber = "316")
public class DisruptingScepter extends Card {

    public DisruptingScepter() {
        // {3}, {T}: Target player discards a card. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{3}, {T}: Target player discards a card. Activate only during your turn.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
