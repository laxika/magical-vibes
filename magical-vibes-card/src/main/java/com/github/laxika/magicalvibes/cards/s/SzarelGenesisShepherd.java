package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "4")
public class SzarelGenesisShepherd extends Card {

    public SzarelGenesisShepherd() {
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());

        PermanentPredicate anotherNontokenPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(anotherCreatureYouControl,
                "Target must be another creature you control"), 0, 1)
                .addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                        new ConditionalTriggeringPermanentEffect(
                                new ControllerTurn(),
                                anotherNontokenPermanent,
                                PutCounterOnTargetPermanentEffect.upToOneTarget(
                                        CounterType.PLUS_ONE_PLUS_ONE, new SourcePower())));
    }
}
