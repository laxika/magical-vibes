package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "246")
public class GazeOfTheGorgon extends Card {

    public GazeOfTheGorgon() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RegenerateEffect(true))
                .addEffect(EffectSlot.SPELL, new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect());
    }
}
