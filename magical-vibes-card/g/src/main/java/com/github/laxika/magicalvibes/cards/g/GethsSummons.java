package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "ONC", collectorNumber = "11")
@CardRegistration(set = "ONC", collectorNumber = "49")
public class GethsSummons extends Card {

    public GethsSummons() {
        CardTypePredicate creatureCards = new CardTypePredicate(CardType.CREATURE);

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(new GraveyardCardPredicateTargetFilter(creatureCards,
                GraveyardSearchScope.ALL_GRAVEYARDS, 3), 0, 99)
                .addEffect(EffectSlot.SPELL,
                        ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyards(creatureCards));
    }
}
