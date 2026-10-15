package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "318")
public class GhostTown extends Card {

    public GhostTown() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."));

        // {0}: Return this land to its owner's hand. Activate only if it's not your turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(ReturnToHandEffect.self()),
                "{0}: Return this land to its owner's hand. Activate only if it's not your turn.",
                null, null, null,
                ActivationTimingRestriction.ONLY_DURING_OPPONENTS_TURN));
    }
}
