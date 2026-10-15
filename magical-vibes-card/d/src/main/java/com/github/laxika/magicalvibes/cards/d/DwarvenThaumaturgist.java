package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "98")
public class DwarvenThaumaturgist extends Card {

    public DwarvenThaumaturgist() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new SwitchPowerToughnessEffect()),
                "{T}: Switch target creature's power and toughness until end of turn."));
    }
}
