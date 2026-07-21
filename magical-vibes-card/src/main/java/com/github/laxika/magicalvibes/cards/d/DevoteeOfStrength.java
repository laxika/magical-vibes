package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "113")
public class DevoteeOfStrength extends Card {

    public DevoteeOfStrength() {
        // {4}{G}: Target creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{4}{G}", List.of(new BoostTargetCreatureEffect(2, 2)),
                "{4}{G}: Target creature gets +2/+2 until end of turn."));
    }
}
