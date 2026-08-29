package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "36")
public class HeidarRimewindMaster extends Card {

    public HeidarRimewindMaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(ReturnToHandEffect.target()),
                "{2}, {T}: Return target permanent to its owner's hand. Activate only if you control four or more snow permanents."
        ).withRequiredControlledPermanents(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                4,
                "four or more snow permanents"));
    }
}
