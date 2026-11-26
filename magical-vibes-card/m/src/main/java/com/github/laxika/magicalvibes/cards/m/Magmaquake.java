package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "140")
@CardRegistration(set = "C14", collectorNumber = "180")
@CardRegistration(set = "C15", collectorNumber = "162")
@CardRegistration(set = "AFC", collectorNumber = "133")
@CardRegistration(set = "C19", collectorNumber = "148")
@CardRegistration(set = "C18", collectorNumber = "125")
@CardRegistration(set = "SCD", collectorNumber = "149")
@CardRegistration(set = "FDC", collectorNumber = "164")
@CardRegistration(set = "CM2", collectorNumber = "112")
public class Magmaquake extends Card {

    public Magmaquake() {
        // "Magmaquake deals X damage to each creature without flying and each planeswalker."
        addSpellEffect(DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                        new PermanentIsPlaneswalkerPredicate()))), new XValue()));
    }
}
