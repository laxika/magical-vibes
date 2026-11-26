package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "176")
@CardRegistration(set = "TPR", collectorNumber = "134")
public class FlowstoneWyvern extends Card {

    public FlowstoneWyvern() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(2, -2)), "{R}: Flowstone Wyvern gets +2/-2 until end of turn."));
    }
}
