package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "184")
@CardRegistration(set = "NEM", collectorNumber = "81")
public class FlowstoneCrusher extends Card {

    public FlowstoneCrusher() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, -1)), "{R}: This creature gets +1/-1 until end of turn."));
    }
}
