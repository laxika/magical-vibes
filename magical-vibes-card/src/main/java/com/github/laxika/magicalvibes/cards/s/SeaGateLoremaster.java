package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "63")
public class SeaGateLoremaster extends Card {

    public SeaGateLoremaster() {
        PermanentCount alliesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ALLY), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DrawCardEffect(alliesYouControl)),
                "{T}: Draw a card for each Ally you control."));
    }
}
