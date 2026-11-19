package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "79")
@CardRegistration(set = "ODY", collectorNumber = "80")
@CardRegistration(set = "DPA", collectorNumber = "6")
public class Deluge extends Card {

    public Deluge() {
        addSpellEffect(new TapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))))));
    }
}
