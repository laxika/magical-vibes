package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "258")
@CardRegistration(set = "ICE", collectorNumber = "211")
public class OrcishSquatters extends Card {

    public OrcishSquatters() {
        // Whenever this creature attacks and isn't blocked, you may gain control of target land
        // defending player controls for as long as you control this creature. If you do, this
        // creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect(
                                new PermanentIsLandPredicate(), ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD, "land"),
                        "You may gain control of target land defending player controls for as long as "
                                + "you control this creature. If you do, this creature assigns no combat damage this turn."));
    }
}
