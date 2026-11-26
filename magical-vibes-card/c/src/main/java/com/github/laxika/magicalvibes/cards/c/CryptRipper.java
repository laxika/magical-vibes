package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "85")
public class CryptRipper extends Card {

    public CryptRipper() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)), "{B}: This creature gets +1/+1 until end of turn."));
    }
}
