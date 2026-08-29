package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOtherCreaturesControlledByTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "101")
public class FlamesOfTheRazeBoar extends Card {

    public FlamesOfTheRazeBoar() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                        new DealDamageToOtherCreaturesControlledByTargetEffect(2)));
    }
}
