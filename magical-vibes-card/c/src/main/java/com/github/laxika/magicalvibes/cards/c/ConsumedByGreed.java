package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "87")
@CardRegistration(set = "FDC", collectorNumber = "92")
public class ConsumedByGreed extends Card {

    public ConsumedByGreed() {
        CardTypePredicate creatureCards = new CardTypePredicate(CardType.CREATURE);

        addStaticEffect(new GiftEffect(1));
        // The gift goes to the chosen opponent, not to the target (CR 702.174a): with two players, the opponent.
        addSpellEffect(new ConditionalEffect(new GiftPromised(), new EachOtherPlayerDrawsCardEffect(1)));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new SacrificePermanentsEffect(
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate())),
                        Subjects.targetPlayer()));
        targetWhenGiftPromised(new GraveyardCardPredicateTargetFilter(
                creatureCards, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1, 1)
                .addSpellEffect(new ConditionalEffect(new GiftPromised(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(creatureCards)
                                .targetGraveyard(true)
                                .upTo(true)
                                .build()));
    }
}
