package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

@CardRegistration(set = "ONC", collectorNumber = "18")
@CardRegistration(set = "ONC", collectorNumber = "56")
public class GlissasRetriever extends Card {

    public GlissasRetriever() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(2)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                ExileGraveyardCardsEffect.cards(Subjects.sourceCard()),
                new ConditionalEffect(HappenedThisWay.exiled(), new QueueReflexiveAbilityEffect(
                        ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(
                                new CardTruePredicate(), new OpponentsWithAtLeastPoisonCounters(3))))));
    }
}
