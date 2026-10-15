package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "114")
@CardRegistration(set = "PIP", collectorNumber = "424")
@CardRegistration(set = "PIP", collectorNumber = "642")
@CardRegistration(set = "PIP", collectorNumber = "952")
public class PaladinElizabethTaggerdy extends Card {

    public PaladinElizabethTaggerdy() {
        // Battalion — Whenever this creature and at least two other creatures attack,
        // draw a card, then you may put a creature card with mana value up to this creature's
        // power from your hand onto the battlefield tapped and attacking.
        CardAllOfPredicate creatureWithinPower = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardManaValueAtMostSourcePowerPredicate()));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new MayEffect(
                                PutCardToBattlefieldEffect.tappedAndAttacking(creatureWithinPower, "creature"),
                                "Put a creature card with mana value up to this creature's power from your hand onto the battlefield tapped and attacking?"))));
    }
}
