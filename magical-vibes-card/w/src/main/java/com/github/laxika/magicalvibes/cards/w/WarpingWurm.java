package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

/**
 * Warping Wurm — {2}{G}{U} Creature — Wurm 1/1.
 * "Phasing"
 * "At the beginning of your upkeep, this creature phases out unless you pay {2}{G}{U}."
 * "Whenever this creature phases in, put a +1/+1 counter on it."
 *
 * <p>Phasing is a printed keyword loaded from Scryfall.
 */
@CardRegistration(set = "MIR", collectorNumber = "287")
public class WarpingWurm extends Card {

    public WarpingWurm() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{2}{G}{U}"),
                        List.of(new PhaseOutEffect(PhaseOutSubject.SOURCE)),
                        true));
        addEffect(EffectSlot.ON_SELF_PHASES_IN, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
