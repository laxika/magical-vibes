package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "88")
public class ShinenOfFearsChill extends Card {

    public ShinenOfFearsChill() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "Channel — {1}{B}, Discard this card: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
