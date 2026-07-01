package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "75")
public class BurrogBanemaker extends Card {

    public BurrogBanemaker() {
        // Deathtouch is applied automatically from the Scryfall keyword.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: This creature gets +1/+1 until end of turn."));
    }
}
