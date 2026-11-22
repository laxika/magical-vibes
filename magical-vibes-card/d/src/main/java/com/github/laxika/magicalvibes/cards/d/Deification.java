package com.github.laxika.magicalvibes.cards.d;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "MAT", collectorNumber = "2")
@CardRegistration(set = "MAT", collectorNumber = "52")
@CardRegistration(set = "MAT", collectorNumber = "102")
@CardRegistration(set = "MAT", collectorNumber = "151")
@CardRegistration(set = "MAT", collectorNumber = "187")
public class Deification extends Card {

    public Deification() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect(CardSubtype.planeswalkerTypes()));
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.permanentsYouControl(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasSourceChosenSubtypePredicate()))));
        addStaticEffect(new PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeEffect());
    }
}
