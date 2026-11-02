package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "95")
public class Taniwha extends Card {

    public Taniwha() {
        // At the beginning of your upkeep, all lands you control phase out. They phase in before you
        // untap during your next untap step (CR 702.26a); trample and phasing come from Scryfall.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new PhaseOutPermanentsEffect(new PermanentIsLandPredicate(), true));
    }
}
