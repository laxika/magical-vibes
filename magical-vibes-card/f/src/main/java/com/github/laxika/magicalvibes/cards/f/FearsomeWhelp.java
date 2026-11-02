package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "40")
public class FearsomeWhelp extends Card {

    public FearsomeWhelp() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PerpetuallyReduceCostForMatchingHandCardsEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON), 1));
    }
}
