package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisCombat;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;

@CardRegistration(set = "ICE", collectorNumber = "296")
public class KjeldoranFrostbeast extends Card {

    public KjeldoranFrostbeast() {
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new SourceAttackedOrBlockedThisCombat(),
                new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect()));
    }
}
