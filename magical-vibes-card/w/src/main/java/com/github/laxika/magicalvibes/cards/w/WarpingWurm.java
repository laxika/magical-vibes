package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


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
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{2}{G}{U}", new PhaseOutEffect(Subjects.source())));
        addTrigger(Triggers.phasesIn(null, ObjectRelation.THIS), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
