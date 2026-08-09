package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EachOpponentGainsLifeCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "118")
public class SkyshroudCutter extends Card {

    public SkyshroudCutter() {
        addCastingOption(new AlternateHandCast(
                List.of(new EachOpponentGainsLifeCastingCost(5)),
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                false));
    }
}
