package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "56")
public class MerfolkThaumaturgist extends Card {

    public MerfolkThaumaturgist() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new SwitchPowerToughnessEffect()),
                "{T}: Switch target creature's power and toughness until end of turn."));
    }
}
