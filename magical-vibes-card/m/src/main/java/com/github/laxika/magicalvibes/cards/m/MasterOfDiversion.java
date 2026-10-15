package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "24")
public class MasterOfDiversion extends Card {

    public MasterOfDiversion() {
        // Whenever Master of Diversion attacks, tap target creature defending player controls.
        // The defending player is the opponent being attacked, matched by the
        // opponent-controlled creature filter (Sidar Jabari).
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ATTACK, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
