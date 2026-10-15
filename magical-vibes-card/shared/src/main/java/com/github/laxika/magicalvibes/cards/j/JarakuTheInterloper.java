package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.c.CallowJushi}.
 */
public class JarakuTheInterloper extends Card {

    public JarakuTheInterloper() {
        // "Remove a ki counter from Jaraku: Counter target spell unless its controller pays {2}."
        // - the ki counters carry over from the unflipped face, so this has no mana cost and no tap.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.KI),
                        new CounterUnlessPaysEffect(2)
                ),
                "Remove a ki counter from Jaraku: Counter target spell unless its controller pays {2}."
        ));
    }
}
