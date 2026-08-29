package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
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
public class ConsumedByGreed extends Card {

    public ConsumedByGreed() {
        CardTypePredicate creatureCards = new CardTypePredicate(CardType.CREATURE);

        addEffect(EffectSlot.STATIC, new GiftEffect());
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new DrawCardForTargetPlayerEffect(1, false, true)))
                .addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate())),
                        SacrificeRecipient.TARGET_PLAYER));
        targetWhenGiftPromised(new GraveyardCardPredicateTargetFilter(
                creatureCards, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(creatureCards)
                                .targetGraveyard(true)
                                .upTo(true)
                                .build()));
    }
}
