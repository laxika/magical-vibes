package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentsAndControllersDrawEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "20")
@CardRegistration(set = "MSC", collectorNumber = "312")
public class KingSolomonsFrogs extends Card {

    public KingSolomonsFrogs() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentMinManaValuePredicate(3),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(new PermanentPredicateTargetFilter(
                targetFilter,
                "Target must be a permanent an opponent controls with mana value 3 or greater"), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new WasCast(), new ExileTargetPermanentsAndControllersDrawEffect()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new ExileSelfCost(), new BecomeMonarchEffect()),
                "{3}, {T}, Exile King Solomon's Frogs: You become the monarch."
        ));
    }
}
