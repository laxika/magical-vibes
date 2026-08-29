package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TopCardOfLibraryManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "156")
public class StormchaserChimera extends Card {

    public StormchaserChimera() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{R}",
                List.of(
                        new ScryEffect(1),
                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                        new BoostSelfEffect(new TopCardOfLibraryManaValue(), new Fixed(0))),
                "{2}{U}{R}: Scry 1, then reveal the top card of your library. This creature gets +X/+0 until end "
                        + "of turn, where X is that card's mana value."));
    }
}
