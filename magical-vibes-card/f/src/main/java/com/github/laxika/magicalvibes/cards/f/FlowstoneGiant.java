package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "174")
public class FlowstoneGiant extends Card {

    public FlowstoneGiant() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(2, -2)), "{R}: Flowstone Giant gets +2/-2 until end of turn."));
    }
}
