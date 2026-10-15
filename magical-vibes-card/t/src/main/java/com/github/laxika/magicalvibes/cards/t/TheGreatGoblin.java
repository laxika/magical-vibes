package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.YouPutCounterOnControlledCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "158")
public class TheGreatGoblin extends Card {

    private static final Set<CardSubtype> GOBLIN_ORC_ARMY = Set.of(
            CardSubtype.GOBLIN, CardSubtype.ORC, CardSubtype.ARMY);

    public TheGreatGoblin() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE,
                new YouPutCounterOnControlledCreatureTriggerEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasAnySubtypePredicate(GOBLIN_ORC_ARMY),
                                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.GOBLIN),
                                new CardSubtypePredicate(CardSubtype.ORC),
                                new CardSubtypePredicate(CardSubtype.ARMY))),
                        new ExileTopCardsMayPlayUntilNextTurnEffect(1)));
    }
}
