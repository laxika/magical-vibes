package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "12")
@CardRegistration(set = "HA4", collectorNumber = "2")
@CardRegistration(set = "SIR", collectorNumber = "23")
public class DeclarationInStone extends Card {

    public DeclarationInStone() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffect());
    }
}
