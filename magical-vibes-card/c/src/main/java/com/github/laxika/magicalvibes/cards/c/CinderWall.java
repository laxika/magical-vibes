package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "182")
@CardRegistration(set = "WTH", collectorNumber = "94")
@CardRegistration(set = "DDG", collectorNumber = "48")
public class CinderWall extends Card {

    public CinderWall() {
        // Defender is a keyword auto-loaded from Scryfall.
        // When this creature blocks, destroy it at end of combat.
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new DestroySelfAtEndOfCombatEffect());
    }
}
