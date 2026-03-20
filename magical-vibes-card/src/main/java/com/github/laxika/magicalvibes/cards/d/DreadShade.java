package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "88")
public class DreadShade extends Card {

    public DreadShade() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 1)), "{B}: Dread Shade gets +1/+1 until end of turn."));
    }
}
