package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "133")
public class LlanowarDruid extends Card {

    public LlanowarDruid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST)))),
                "{T}, Sacrifice Llanowar Druid: Untap all Forests."
        ));
    }
}
