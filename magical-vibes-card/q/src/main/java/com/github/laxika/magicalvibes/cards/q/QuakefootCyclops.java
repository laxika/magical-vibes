package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "142")
public class QuakefootCyclops extends Card {

    public QuakefootCyclops() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET), new DrawCardEffect(1)),
                "Cycling {1}{R} ({1}{R}, Discard this card: Draw a card.)",
                TargetFilters.creature()));
    }
}
