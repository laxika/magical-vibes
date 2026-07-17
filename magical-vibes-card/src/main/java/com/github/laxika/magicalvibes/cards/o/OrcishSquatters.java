package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "258")
public class OrcishSquatters extends Card {

    public OrcishSquatters() {
        // Whenever this creature attacks and isn't blocked, you may gain control of target land
        // defending player controls for as long as you control this creature. If you do, this
        // creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect(),
                        "You may gain control of target land defending player controls for as long as "
                                + "you control this creature. If you do, this creature assigns no combat damage this turn."));
    }
}
