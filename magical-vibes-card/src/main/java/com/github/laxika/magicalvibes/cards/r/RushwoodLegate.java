package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "266")
public class RushwoodLegate extends Card {

    public RushwoodLegate() {
        addCastingOption(new AlternateHandCast(
                List.of(),
                new AllConditions(List.of(
                        new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)))),
                false));
    }
}
