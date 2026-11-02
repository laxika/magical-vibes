package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreaturesDealToughnessDamageToEachOtherEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import static com.github.laxika.magicalvibes.model.filter.TargetFilters.creatureAnOpponentControls;
import static com.github.laxika.magicalvibes.model.filter.TargetFilters.creatureYouControl;

@CardRegistration(set = "MOC", collectorNumber = "53")
public class TheGreatAerie extends Card {

    public TheGreatAerie() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new BolsterEffect(3));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new BolsterEffect(3));

        target(creatureYouControl(), 0, 1);
        target(creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new TargetCreaturesDealToughnessDamageToEachOtherEffect());
    }
}
