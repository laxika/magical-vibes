package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.cards.b.BlackLotus;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GarthOneEyeEffect;

import java.util.List;

@CardRegistration(set = "SLZ", collectorNumber = "90")
@CardRegistration(set = "SLZ", collectorNumber = "211")
@CardRegistration(set = "SLZ", collectorNumber = "332")
public class GarthOneEye extends Card {

    public GarthOneEye() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new GarthOneEyeEffect(List.of(
                        new Disenchant(),
                        new Braingeyser(),
                        new Terror(),
                        new ShivanDragon(),
                        new Regrowth(),
                        new BlackLotus()))),
                "{T}: Choose a card name that hasn't been chosen from among Disenchant, Braingeyser, Terror, Shivan Dragon, Regrowth, and Black Lotus. Create a copy of the card with the chosen name. You may cast the copy. (You still pay its costs.)"));
    }
}
