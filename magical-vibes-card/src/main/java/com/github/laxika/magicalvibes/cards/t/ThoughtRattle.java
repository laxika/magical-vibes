package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyReduceSoughtCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "30")
public class ThoughtRattle extends Card {

    public ThoughtRattle() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL,
                        new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND),
                                HandChoiceDestination.EXILE))
                .addEffect(EffectSlot.SPELL,
                        new ConditionalEffect(new GraveyardCardThreshold(7, null),
                                new SeekLibraryAndPerpetuallyReduceSoughtCardEffect(
                                        new CardSubtypePredicate(CardSubtype.RAT))))
                .addEffect(EffectSlot.SPELL,
                        new GainLifeEffect(new MatchingCardsInHand(
                                CountScope.CONTROLLER, new CardSubtypePredicate(CardSubtype.RAT))));
    }
}
