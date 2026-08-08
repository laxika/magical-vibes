package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.f.FaithfulSquire}.
 */
public class KaisoMemoryOfLoyalty extends Card {

    public KaisoMemoryOfLoyalty() {
        // "Remove a ki counter from Kaiso: Prevent all damage that would be dealt to target creature
        // this turn." - the ki counters carry over from the unflipped face, so this has no mana cost
        // and no tap.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.KI),
                        PreventDamageEffect.allToTargetCreatures()
                ),
                "Remove a ki counter from Kaiso: Prevent all damage that would be dealt to target "
                        + "creature this turn."
        ));
    }
}
