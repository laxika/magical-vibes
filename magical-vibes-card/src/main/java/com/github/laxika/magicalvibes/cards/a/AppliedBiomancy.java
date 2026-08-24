package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "153")
public class AppliedBiomancy extends Card {

    public AppliedBiomancy() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +1/+1 until end of turn",
                        new BoostTargetCreatureEffect(1, 1),
                        creature),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature to its owner's hand",
                        ReturnToHandEffect.target(),
                        creature)
        )));
    }
}
