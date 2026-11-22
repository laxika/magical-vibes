package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "51")
public class FirefluxSquad extends Card {

    public FirefluxSquad() {
        var anotherAttackingCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                anotherAttackingCreatureYouControl,
                "Target must be another attacking creature you control"))
                .addTrigger(Triggers.attacks(), new MayEffect(
                        SequenceEffect.of(
                                new ExileTargetPermanentEffect(anotherAttackingCreatureYouControl),
                                new ConditionalEffect(HappenedThisWay.exiled(),
                                        RevealUntilCardPredicateRestOnBottomRandomEffect.tappedAndAttacking(
                                                new CardTypePredicate(CardType.CREATURE)))),
                        "Exile target attacking creature?"));
    }
}
