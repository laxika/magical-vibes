package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "139")
public class CabalCoffers extends Card {

    public CabalCoffers() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardManaEffect(
                        ManaColor.BLACK,
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER)
                )),
                "{2}, {T}: Add {B} for each Swamp you control."
        ));
    }
}
