package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "POR", collectorNumber = "8")
@CardRegistration(set = "MIR", collectorNumber = "5")
@CardRegistration(set = "INV", collectorNumber = "9")
@CardRegistration(set = "S99", collectorNumber = "8")
public class BlindingLight extends Card {

    public BlindingLight() {
        addSpellEffect(new TapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.WHITE))))))));
    }
}
