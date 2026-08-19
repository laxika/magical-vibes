package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "28")
public class AlexiZephyrMage extends Card {

    public AlexiZephyrMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{U}",
                List.of(new DiscardCardTypeCost(null, null, 2), ReturnToHandEffect.target()),
                "{X}{U}, {T}, Discard two cards: Return X target creatures to their owners' hands.",
                TargetFilters.creature(),
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withXScaledTargets());
    }
}
