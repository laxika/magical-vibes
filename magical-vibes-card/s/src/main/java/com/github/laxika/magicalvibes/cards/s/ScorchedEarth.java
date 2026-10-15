package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "TMP", collectorNumber = "200")
public class ScorchedEarth extends Card {

    public ScorchedEarth() {
        // Additional cost: discard X land cards (the same X the spell is cast for).
        addEffect(EffectSlot.SPELL, new DiscardXCardsCost(
                new CardTypePredicate(CardType.LAND), "land cards"));

        // Destroy X target lands: one X-scaled target group, every chosen target destroyed.
        targetX(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Targets must be lands"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
