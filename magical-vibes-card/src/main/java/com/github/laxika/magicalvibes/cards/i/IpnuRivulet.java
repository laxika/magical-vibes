package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "180")
public class IpnuRivulet extends Card {

    public IpnuRivulet() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}, Pay 1 life: Add {U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.BLUE)),
                "{T}, Pay 1 life: Add {U}."
        ));

        // {1}{U}, {T}, Sacrifice a Desert: Target player mills four cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                                "Sacrifice a Desert",
                                false),
                        new MillEffect(4, MillRecipient.TARGET_PLAYER)),
                "{1}{U}, {T}, Sacrifice a Desert: Target player mills four cards."
        ));
    }
}
