package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "100")
public class SaprazzanLegate extends Card {

    public SaprazzanLegate() {
        addCastingOption(new AlternateHandCast(
                List.of(),
                new AllConditions(List.of(
                        new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)))),
                false));
    }
}
