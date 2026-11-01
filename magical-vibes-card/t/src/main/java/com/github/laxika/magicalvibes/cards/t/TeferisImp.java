package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Teferi's Imp — {2}{U} Creature — Imp 1/1.
 * "Flying"
 * "Phasing"
 * "Whenever this creature phases out, discard a card."
 * "Whenever this creature phases in, draw a card."
 *
 * <p>Flying and phasing are printed keywords loaded from Scryfall; the phase-out trigger is
 * collected before the permanent leaves the battlefield because such abilities look back in time
 * (CR 603.10b).
 */
@CardRegistration(set = "MIR", collectorNumber = "98")
public class TeferisImp extends Card {

    public TeferisImp() {
        addTrigger(Triggers.phasesOut(null, ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addTrigger(Triggers.phasesIn(null, ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
