package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "721")
public class Flatman extends Card {

    public Flatman() {
        // {2}{G}: Switch Flatman's power and toughness until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(new SwitchPowerToughnessEffect(true)),
                "{2}{G}: Switch Flatman's power and toughness until end of turn."));
    }
}
