package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Inherited Fiend's back face.
 */
public class InheritedFiend extends Card {

    public InheritedFiend() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                                1, new CardTypePredicate(CardType.CREATURE), false),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "{2}{B}: Exile target creature card from a graveyard. Put a +1/+1 counter on this creature."
        ));
    }
}
