package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "71")
public class Equilibrium extends Card {

    public Equilibrium() {
        // Whenever you cast a creature spell, you may pay {1}. If you do, return target creature to
        // its owner's hand. The MayEffect models the "you may pay {1}" decision (accepting pays the
        // cost, then a target is chosen from the card's filter — see MayAbilityHandlerService).
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(ReturnToHandEffect.target()),
                        "{1}"),
                "Pay {1} to return target creature to its owner's hand?"
        ));
    }
}
