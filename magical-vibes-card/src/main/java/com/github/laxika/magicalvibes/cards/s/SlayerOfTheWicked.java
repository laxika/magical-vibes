package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "32")
public class SlayerOfTheWicked extends Card {

    public SlayerOfTheWicked() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.VAMPIRE, CardSubtype.WEREWOLF, CardSubtype.ZOMBIE)),
                "Target must be a Vampire, Werewolf, or Zombie"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new DestroyTargetPermanentEffect(),
                                "Destroy target Vampire, Werewolf, or Zombie?"));
    }
}
