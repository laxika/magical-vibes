package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "39")
@CardRegistration(set = "VMA", collectorNumber = "45")
public class SidarJabari extends Card {

    public SidarJabari() {
        // Whenever Sidar Jabari attacks, tap target creature defending player controls.
        // Flanking is a Scryfall-loaded keyword. The defending player is the opponent being
        // attacked, matched by the opponent-controlled creature filter (Territorial Hammerskull).
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.attacks(), new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
