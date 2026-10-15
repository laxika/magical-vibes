package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MIR", collectorNumber = "71")
public class KukemssaPirates extends Card {

    public KukemssaPirates() {
        // Whenever this creature attacks and isn't blocked, you may gain control of target artifact
        // defending player controls. If you do, this creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect(
                                new PermanentIsArtifactPredicate(), ControlDuration.PERMANENT, "artifact"),
                        "You may gain control of target artifact defending player controls. "
                                + "If you do, this creature assigns no combat damage this turn."));
    }
}
