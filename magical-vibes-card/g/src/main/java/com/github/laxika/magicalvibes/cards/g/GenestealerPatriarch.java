package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "22")
public class GenestealerPatriarch extends Card {

    public GenestealerPatriarch() {
        var defendingPlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()));
        target(new PermanentPredicateTargetFilter(
                defendingPlayerCreature,
                "Target must be a creature defending player controls"))
                .addTrigger(Triggers.attacks(),
                        new PutCounterOnTargetPermanentEffect(CounterType.INFECTION));

        CardEffect childrenOfTheCult = new CreateTokenCopyOfDyingCreatureEffect(
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(CardSubtype.TYRANID), Set.of(), null, null, Map.of()));
        addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.INFECTION), ObjectRelation.ANY),
                childrenOfTheCult);
    }
}
