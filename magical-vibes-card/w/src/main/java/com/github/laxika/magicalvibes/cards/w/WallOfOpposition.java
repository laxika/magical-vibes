package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "56")
@CardRegistration(set = "LEG", collectorNumber = "171")
public class WallOfOpposition extends Card {

    public WallOfOpposition() {
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new BoostSelfEffect(1, 0)), "{1}: This creature gets +1/+0 until end of turn."));
    }
}
