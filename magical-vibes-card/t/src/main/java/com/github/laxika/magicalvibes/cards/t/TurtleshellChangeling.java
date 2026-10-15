package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "94")
public class TurtleshellChangeling extends Card {

    public TurtleshellChangeling() {
        // Changeling keyword is auto-loaded from Scryfall.
        // {1}{U}: Switch this creature's power and toughness until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new SwitchPowerToughnessEffect(true)),
                "{1}{U}: Switch this creature's power and toughness until end of turn."));
    }
}
