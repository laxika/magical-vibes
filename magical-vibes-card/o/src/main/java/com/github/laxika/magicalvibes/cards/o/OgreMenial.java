package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "89")
public class OgreMenial extends Card {

    public OgreMenial() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 0)), "{R}: Ogre Menial gets +1/+0 until end of turn."));
    }
}
