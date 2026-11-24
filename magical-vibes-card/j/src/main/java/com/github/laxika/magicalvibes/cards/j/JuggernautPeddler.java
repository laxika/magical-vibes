package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "23")
public class JuggernautPeddler extends Card {

    public JuggernautPeddler() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.EXILE)
                                .withRevealMatchingOnly()
                                .withUpTo(true),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                ConjureCardsEffect.named("10E", "328", ConjureDestination.intoHand())
                                        .conjuredBy(Subjects.targetPlayerDeclaredElsewhere()))));
    }
}
