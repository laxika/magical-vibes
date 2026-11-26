package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "11")
@CardRegistration(set = "UDS", collectorNumber = "3")
public class CapashenKnight extends Card {

    public CapashenKnight() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(BoostEffect.self(1, 0)), "{1}{W}: This creature gets +1/+0 until end of turn."));
    }
}
