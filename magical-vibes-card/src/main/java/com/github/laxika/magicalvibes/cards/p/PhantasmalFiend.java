package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "57a")
@CardRegistration(set = "ALL", collectorNumber = "57b")
@CardRegistration(set = "DKM", collectorNumber = "8a")
@CardRegistration(set = "DKM", collectorNumber = "8b")
public class PhantasmalFiend extends Card {

    public PhantasmalFiend() {
        // {B}: This creature gets +1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new BoostSelfEffect(1, -1)),
                "{B}: This creature gets +1/-1 until end of turn."));

        // {1}{U}: Switch this creature's power and toughness until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new SwitchPowerToughnessEffect(true)),
                "{1}{U}: Switch this creature's power and toughness until end of turn."));
    }
}
