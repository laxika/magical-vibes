package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "106")
public class StromkirkCondemned extends Card {

    public StromkirkCondemned() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostAllOwnCreaturesEffect(1, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE))
                ),
                "Discard a card: Vampires you control get +1/+1 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
