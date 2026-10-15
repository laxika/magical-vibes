package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "78")
public class BolaWarrior extends Card {

    public BolaWarrior() {
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{R}, {T}, Discard a card: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
