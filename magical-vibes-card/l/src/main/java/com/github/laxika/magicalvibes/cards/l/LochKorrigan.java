package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "71")
public class LochKorrigan extends Card {

    public LochKorrigan() {
        addActivatedAbility(new ActivatedAbility(false, "{U/B}", List.of(new BoostSelfEffect(1, 1)), "{U/B}: This creature gets +1/+1 until end of turn."));
    }
}
