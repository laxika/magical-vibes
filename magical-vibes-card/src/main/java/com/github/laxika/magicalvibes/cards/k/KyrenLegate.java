package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "197")
public class KyrenLegate extends Card {

    public KyrenLegate() {
        addCastingOption(new AlternateHandCast(
                List.of(),
                new AllConditions(List.of(
                        new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))),
                false));
    }
}
