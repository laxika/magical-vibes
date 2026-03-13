package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "135")
@CardRegistration(set = "M11", collectorNumber = "136")
public class FieryHellhound extends Card {

    public FieryHellhound() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)), "{R}: Fiery Hellhound gets +1/+0 until end of turn."));
    }
}
