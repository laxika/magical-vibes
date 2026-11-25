package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "105")
@CardRegistration(set = "SPM", collectorNumber = "226")
@CardRegistration(set = "OM1", collectorNumber = "121")
public class KravensLastHunt extends Card {

    public KravensLastHunt() {
        addTrigger(Triggers.chapter(1), SequenceEffect.of(new MillEffect(5, Subjects.you()),
                new ConditionalEffect(HappenedThisWay.atLeast(StepKind.MILL, 5),
                        new QueueReflexiveAbilityEffect(DealDamageEffect.toTargetCreature(new GreatestPowerAmongCardsInGraveyard(
                                        new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER))))));

        addTrigger(Triggers.chapter(2), new BoostTargetCreatureEffect(2, 2));
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));

        addTrigger(Triggers.chapter(3), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
    }
}
