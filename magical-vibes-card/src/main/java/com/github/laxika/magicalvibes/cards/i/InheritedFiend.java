package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect;

import java.util.List;

/**
 * Inherited Fiend's back face.
 */
public class InheritedFiend extends Card {

    public InheritedFiend() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect()),
                "{2}{B}: Exile target creature card from a graveyard. Put a +1/+1 counter on this creature."
        ));
    }
}
