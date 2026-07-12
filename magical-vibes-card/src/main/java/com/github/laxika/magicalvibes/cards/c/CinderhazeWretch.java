package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "60")
public class CinderhazeWretch extends Card {

    public CinderhazeWretch() {
        // {T}: Target player discards a card. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{T}: Target player discards a card. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));

        // Put a -1/-1 counter on this creature: Untap this creature.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PutCounterOnSourceCost(), new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Put a -1/-1 counter on this creature: Untap this creature."));
    }
}
