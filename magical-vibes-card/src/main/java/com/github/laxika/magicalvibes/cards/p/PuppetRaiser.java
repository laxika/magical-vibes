package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardThenSeekWithMenaceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "YMID", collectorNumber = "31")
public class PuppetRaiser extends Card {

    public PuppetRaiser() {
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new ExileTargetCreatureCardFromGraveyardThenSeekWithMenaceEffect());
    }
}
