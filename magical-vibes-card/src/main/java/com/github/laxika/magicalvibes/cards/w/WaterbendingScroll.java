package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "81")
public class WaterbendingScroll extends Card {

    public WaterbendingScroll() {
        var islands = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new ReduceActivationCostEffect(islands), new DrawCardEffect(1)),
                "{6}, {T}: Draw a card. This ability costs {1} less to activate for each Island you control."
        ));
    }
}
