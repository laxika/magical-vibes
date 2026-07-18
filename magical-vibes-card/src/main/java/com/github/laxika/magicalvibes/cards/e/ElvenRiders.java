package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "259")
@CardRegistration(set = "6ED", collectorNumber = "225")
@CardRegistration(set = "5ED", collectorNumber = "291")
@CardRegistration(set = "4ED", collectorNumber = "242")
public class ElvenRiders extends Card {

    public ElvenRiders() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentHasSubtypePredicate(CardSubtype.WALL)
                )),
                "creatures with flying or Walls"
        ));
    }
}
