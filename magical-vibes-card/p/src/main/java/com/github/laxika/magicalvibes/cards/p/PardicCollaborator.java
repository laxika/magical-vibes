package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "106")
public class PardicCollaborator extends Card {

    public PardicCollaborator() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."));
    }
}
