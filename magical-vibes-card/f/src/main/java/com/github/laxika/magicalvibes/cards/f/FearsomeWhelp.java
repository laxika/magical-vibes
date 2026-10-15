package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YMID", collectorNumber = "40")
public class FearsomeWhelp extends Card {

    public FearsomeWhelp() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PerpetuallyReduceCostForMatchingHandCardsEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON), 1));
    }
}
