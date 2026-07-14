package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "101")
public class CragPuca extends Card {

    public CragPuca() {
        // {U/R}: Switch this creature's power and toughness until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{U/R}",
                List.of(new SwitchPowerToughnessEffect(true)),
                "{U/R}: Switch this creature's power and toughness until end of turn."));
    }
}
