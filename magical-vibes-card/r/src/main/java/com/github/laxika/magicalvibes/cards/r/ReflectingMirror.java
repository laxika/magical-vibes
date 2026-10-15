package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "106")
public class ReflectingMirror extends Card {

    public ReflectingMirror() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(
                        new IncreaseActivationCostEffect(new Scaled(new TargetSpellManaValue(), 2)),
                        ChangeTargetOfTargetSpellWithSingleTargetEffect.playersOnly()
                ),
                "{X}, {T}: Change the target of target spell with a single target if that target is you. The new target must be a player. X is twice the mana value of that spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryIsSingleTargetPredicate(),
                                new StackEntryTargetsYouPredicate()
                        )),
                        "Target must be a single-target spell that targets you."
                )
        ));
    }
}
