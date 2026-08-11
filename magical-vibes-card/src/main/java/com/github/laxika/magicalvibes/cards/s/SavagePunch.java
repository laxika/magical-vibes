package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "147")
public class SavagePunch extends Card {

    public SavagePunch() {
        // Ferocious — the creature you control gets +2/+2 until end of turn before it fights.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                        new BoostTargetCreatureEffect(2, 2)));

        // Target creature you control fights target creature you don't control.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new FightTargetsEffect());
    }
}
