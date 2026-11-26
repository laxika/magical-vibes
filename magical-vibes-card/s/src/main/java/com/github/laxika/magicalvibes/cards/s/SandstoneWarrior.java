package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "215")
@CardRegistration(set = "TMP", collectorNumber = "199")
@CardRegistration(set = "TPR", collectorNumber = "154")
public class SandstoneWarrior extends Card {

    public SandstoneWarrior() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 0)), "{R}: This creature gets +1/+0 until end of turn."));
    }
}
