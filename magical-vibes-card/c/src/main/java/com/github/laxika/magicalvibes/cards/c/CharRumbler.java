package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "96")
@CardRegistration(set = "TSR", collectorNumber = "158")
public class CharRumbler extends Card {

    public CharRumbler() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 0)),
                "{R}: Char-Rumbler gets +1/+0 until end of turn."));
    }
}
