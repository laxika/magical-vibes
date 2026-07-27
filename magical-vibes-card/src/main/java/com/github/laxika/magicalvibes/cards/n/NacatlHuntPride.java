package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockThisTurnIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "13")
public class NacatlHuntPride extends Card {

    public NacatlHuntPride() {
        // Vigilance is auto-loaded from Scryfall.

        // {R}, {T}: Target creature can't block this turn.
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{R}, {T}: Target creature can't block this turn.",
                TargetFilters.creature()));

        // {G}, {T}: Target creature blocks this turn if able.
        addActivatedAbility(new ActivatedAbility(true, "{G}",
                List.of(new MustBlockThisTurnIfAbleEffect()),
                "{G}, {T}: Target creature blocks this turn if able.",
                TargetFilters.creature()));
    }
}
