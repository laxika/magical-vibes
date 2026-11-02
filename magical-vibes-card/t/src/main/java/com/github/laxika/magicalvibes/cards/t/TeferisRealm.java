package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutChosenTypeNontokenPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "44")
public class TeferisRealm extends Card {

    public TeferisRealm() {
        // Triggers.upkeep(ActivePlayer.ANY) puts the active player on targetId so that player chooses the type
        // (not this enchantment's controller). World rule is handled by SBA.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new PhaseOutChosenTypeNontokenPermanentsEffect());
    }
}
