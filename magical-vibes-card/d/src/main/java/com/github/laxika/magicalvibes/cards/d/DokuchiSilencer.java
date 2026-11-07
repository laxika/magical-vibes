package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "95")
public class DokuchiSilencer extends Card {

    public DokuchiSilencer() {
        addNinjutsu("{1}{B}");

        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                new DiscardCardThenEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                new PermanentControlledByTriggeringPlayerPredicate()))),
                        "a creature card"),
                "Discard a creature card to destroy target creature or planeswalker that player controls?"));
    }
}
