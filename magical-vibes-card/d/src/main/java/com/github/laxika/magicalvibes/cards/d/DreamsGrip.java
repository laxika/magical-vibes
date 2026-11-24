package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "34")
public class DreamsGrip extends Card {

    public DreamsGrip() {
        addCastingCost(EscalateCost.entwine("{1}"));
        setAllowSharedTargets(true);
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target permanent",
                        new TapPermanentsEffect(Subjects.target()),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap target permanent",
                        new UntapPermanentsEffect(Subjects.target()),
                        TargetFilters.permanent())
        )));
    }
}
