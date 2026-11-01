package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.PreventPhaseOutEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Ertai's Familiar — {1}{U} Creature — Illusion 2/2.
 * "Phasing"
 * "When this creature phases out or leaves the battlefield, mill three cards."
 * "{U}: Until your next upkeep, this creature can't phase out."
 *
 * <p>Phasing is a printed keyword loaded from Scryfall. Phasing out is not a zone change
 * (CR 702.26d), so the two halves of the trigger are two separate slots — the same mill effect on
 * ON_SELF_PHASES_OUT and its leaves-the-battlefield trigger — and only one of them can ever fire for a
 * given event.
 */
@CardRegistration(set = "WTH", collectorNumber = "38")
public class ErtaisFamiliar extends Card {

    public ErtaisFamiliar() {
        addTrigger(Triggers.phasesOut(null, ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new PreventPhaseOutEffect(PhaseOutSubject.SOURCE)),
                "{U}: Until your next upkeep, this creature can't phase out."));
    }
}
