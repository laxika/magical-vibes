package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "25")
public class BalshanCollaborator extends Card {

    public BalshanCollaborator() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."));
    }
}
