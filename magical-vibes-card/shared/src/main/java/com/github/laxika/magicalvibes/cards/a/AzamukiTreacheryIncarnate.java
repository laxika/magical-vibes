package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.c.CunningBandit}.
 */
public class AzamukiTreacheryIncarnate extends Card {

    public AzamukiTreacheryIncarnate() {
        // "Remove a ki counter from Azamuki: Gain control of target creature until end of turn."
        // - the ki counters carry over from the unflipped face, so this has no mana cost and no tap.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.KI),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN)
                ),
                "Remove a ki counter from Azamuki: Gain control of target creature until end of turn.",
                TargetFilters.creature()
        ));
    }
}
