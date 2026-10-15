package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "22")
public class SafeholdSentry extends Card {

    public SafeholdSentry() {
        // {2}{W}, {Q}: This creature gets +0/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{W}",
                List.of(new BoostSelfEffect(0, 2)),
                "{2}{W}, {Q}: This creature gets +0/+2 until end of turn."
        ).withRequiresUntap());
    }
}
