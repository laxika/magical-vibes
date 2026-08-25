package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "224")
public class JoustingDummy extends Card {

    public JoustingDummy() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new BoostSelfEffect(1, 0)), "{3}: This creature gets +1/+0 until end of turn."));
    }
}
