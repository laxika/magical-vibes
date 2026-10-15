package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "43")
@CardRegistration(set = "PIP", collectorNumber = "571")
public class ButchDeLoriaTunnelSnake extends Card {

    public ButchDeLoriaTunnelSnake() {
        PermanentPredicate rogueOrSnake = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.ROGUE),
                new PermanentHasSubtypePredicate(CardSubtype.SNAKE)));
        PermanentCount otherRoguesAndSnakes = new PermanentCount(rogueOrSnake, CountScope.CONTROLLER, true);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(otherRoguesAndSnakes, otherRoguesAndSnakes));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.MENACE),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.ROGUE)
                ),
                "{1}{B}: Put a menace counter on another target creature. It becomes a Rogue in addition to its other types.",
                new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature")
        ));
    }
}
