package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "18")
public class SteelingStance extends Card {

    public SteelingStance() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "Forecast — {W}, Reveal this card from your hand: Target creature gets +1/+1 until end of turn. "
                        + "Activate only during your upkeep and only once each turn.",
                TargetFilters.creature(),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
