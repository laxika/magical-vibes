package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "106")
@CardRegistration(set = "C13", collectorNumber = "138")
public class BroodingSaurian extends Card {

    public BroodingSaurian() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    }
}
