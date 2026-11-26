package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "223")
public class WallOfLava extends Card {

    public WallOfLava() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 1)), "{R}: This creature gets +1/+1 until end of turn."));
    }
}
