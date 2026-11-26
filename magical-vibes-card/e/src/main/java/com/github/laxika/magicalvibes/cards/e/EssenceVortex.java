package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ICE", collectorNumber = "287")
public class EssenceVortex extends Card {

    public EssenceVortex() {
        // "Destroy target creature unless its controller pays life equal to its toughness.
        //  A creature destroyed this way can't be regenerated."
        target(TargetFilters.creature()).addSpellEffect(UnlessPaysEffect.unlessPays(
                Subjects.controllerOf(Subjects.target()), new PayLifeCost(new TargetToughness()),
                new DestroyTargetPermanentEffect(Subjects.target(), true)));
    }
}
