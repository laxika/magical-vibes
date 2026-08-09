package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "161")
public class SlumberingTora extends Card {

    public SlumberingTora() {
        LastDiscardedCardManaValue discardedManaValue = new LastDiscardedCardManaValue();
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DiscardCardTypeCost(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE))), "Spirit or Arcane"),
                        new AnimatePermanentsEffect(
                                discardedManaValue,
                                discardedManaValue,
                                List.of(CardSubtype.CAT),
                                Set.of(),
                                null,
                                Set.of(),
                                GrantScope.SELF,
                                EffectDuration.UNTIL_END_OF_TURN,
                                null)),
                "{2}, Discard a Spirit or Arcane card: This artifact becomes an X/X Cat artifact creature until end of turn, where X is the discarded card's mana value."
        ));
    }
}
