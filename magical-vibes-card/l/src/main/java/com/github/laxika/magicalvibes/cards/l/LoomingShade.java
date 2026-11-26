package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "145")
@CardRegistration(set = "8ED", collectorNumber = "140")
@CardRegistration(set = "9ED", collectorNumber = "142")
@CardRegistration(set = "10E", collectorNumber = "153")
@CardRegistration(set = "M10", collectorNumber = "103")
@CardRegistration(set = "USG", collectorNumber = "139")
public class LoomingShade extends Card {

    public LoomingShade() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)), "{B}: Looming Shade gets +1/+1 until end of turn."));
    }
}
