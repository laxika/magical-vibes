package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "197")
public class LlanowarVanguard extends Card {

    public LlanowarVanguard() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new BoostSelfEffect(0, 4)), "{T}: This creature gets +0/+4 until end of turn."));
    }
}
