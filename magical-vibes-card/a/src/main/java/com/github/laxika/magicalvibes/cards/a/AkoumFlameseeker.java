package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "101")
public class AkoumFlameseeker extends Card {

    public AkoumFlameseeker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ALLY), true, false),
                        new DiscardAndDrawCardEffect()
                ),
                "{T}, Tap an untapped Ally you control: Discard a card. If you do, draw a card."
        ));
    }
}
