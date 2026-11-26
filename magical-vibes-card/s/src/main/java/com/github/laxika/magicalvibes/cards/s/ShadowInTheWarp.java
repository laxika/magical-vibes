package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "140")
public class ShadowInTheWarp extends Card {

    public ShadowInTheWarp() {
        addStaticEffect(new ReduceCastCostForFirstMatchingSpellEachTurnEffect(
                new CardTypePredicate(CardType.CREATURE), 2));

        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                        .nthThisTurn(1),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.targetSpell()), 2));
    }
}
