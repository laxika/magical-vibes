package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "32")
@CardRegistration(set = "DDQ", collectorNumber = "18")
@CardRegistration(set = "INR", collectorNumber = "39")
@CardRegistration(set = "INR", collectorNumber = "348")
public class SlayerOfTheWicked extends Card {

    public SlayerOfTheWicked() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.VAMPIRE, CardSubtype.WEREWOLF, CardSubtype.ZOMBIE)),
                "Target must be a Vampire, Werewolf, or Zombie"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(new DestroyTargetPermanentEffect(),
                                "Destroy target Vampire, Werewolf, or Zombie?"));
    }
}
