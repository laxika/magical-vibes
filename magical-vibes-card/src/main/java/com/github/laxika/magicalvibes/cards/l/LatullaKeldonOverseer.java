package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "95")
public class LatullaKeldonOverseer extends Card {

    public LatullaKeldonOverseer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "{X}{R}, {T}, Discard two cards: Latulla deals X damage to any target."
        ));
    }
}
