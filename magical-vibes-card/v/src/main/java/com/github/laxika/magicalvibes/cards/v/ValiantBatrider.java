package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTargetPlayerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "4")
public class ValiantBatrider extends Card {

    public ValiantBatrider() {
        // When the damaged player next casts a noncreature spell, they may pay {1}. If they do
        // not, each opponent draws a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                RegisterDelayedTargetPlayerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new MayPayManaEffect(
                                "{1}", null,
                                "Pay {1} to stop each opponent from drawing a card?",
                                new EachOpponentDrawsCardEffect(1)))));
    }
}
