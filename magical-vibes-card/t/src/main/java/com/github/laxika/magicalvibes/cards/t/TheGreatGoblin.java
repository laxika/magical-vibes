package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        )).addTrigger(Triggers.youPutCountersOnCreatureYouControl()
                        .withFilter(new PermanentHasAnySubtypePredicate(GOBLIN_ORC_ARMY)),
                DealDamageEffect.to(Subjects.targetPlayer(), 2));

        addTrigger(Triggers.creatureDies(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                                new PermanentHasSubtypePredicate(CardSubtype.ORC),
                                new PermanentHasSubtypePredicate(CardSubtype.ARMY))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
