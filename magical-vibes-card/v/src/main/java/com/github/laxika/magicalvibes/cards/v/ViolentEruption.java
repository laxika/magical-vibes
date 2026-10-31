package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "TOR", collectorNumber = "117")
@CardRegistration(set = "C19", collectorNumber = "154")
public class ViolentEruption extends Card {

    public ViolentEruption() {
        // Violent Eruption deals 4 damage divided as you choose among any number of targets.
        addSpellEffect(DealDividedDamageEffect.chosenAmongAnyTargets(4));

        // Madness {1}{R}{R}
        addCastingOption(new MadnessCast("{1}{R}{R}"));
    }
}
