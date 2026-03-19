package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "212")
public class BloodtallowCandle extends Card {

    public BloodtallowCandle() {
        // {6}, {T}, Sacrifice Bloodtallow Candle: Target creature gets -5/-5 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-5, -5)),
                "{6}, {T}, Sacrifice Bloodtallow Candle: Target creature gets -5/-5 until end of turn."
        ));
    }
}
