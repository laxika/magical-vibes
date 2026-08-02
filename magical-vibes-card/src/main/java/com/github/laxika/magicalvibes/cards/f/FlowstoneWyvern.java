package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "176")
public class FlowstoneWyvern extends Card {

    public FlowstoneWyvern() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(2, -2)), "{R}: Flowstone Wyvern gets +2/-2 until end of turn."));
    }
}
