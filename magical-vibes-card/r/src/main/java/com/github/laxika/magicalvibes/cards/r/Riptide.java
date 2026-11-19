package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "DRK", collectorNumber = "35")
public class Riptide extends Card {

    public Riptide() {
        addSpellEffect(new TapPermanentsEffect(
                Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE)))))));
    }
}
