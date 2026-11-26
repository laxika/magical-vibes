package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "84")
public class VectisDominator extends Card {

    public VectisDominator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(UnlessPaysEffect.unlessPaysLife(Subjects.controllerOf(Subjects.target()), 2,
                        new TapPermanentsEffect(Subjects.target()))),
                "{T}: Tap target creature unless its controller pays 2 life.",
                TargetFilters.creature()
        ));
    }
}
