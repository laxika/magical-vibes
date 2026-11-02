package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfTheCouncilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "596")
public class PrimeMinistersCabinetRoom extends Card {

    public PrimeMinistersCabinetRoom() {
        // At the beginning of combat on your turn, up to one target creature you control becomes a
        // copy of target creature an opponent controls.
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new MakeTargetCopyOfTargetPermanentEffect());
        target(TargetFilters.creatureAnOpponentControls());

        // Will of the council — Whenever chaos ensues, starting with you, each player votes for a
        // creature you don't control. Exile each creature with the most votes or tied for most votes.
        addEffect(EffectSlot.CHAOS_TRIGGERED, new WillOfTheCouncilEffect(false, true));
    }
}
