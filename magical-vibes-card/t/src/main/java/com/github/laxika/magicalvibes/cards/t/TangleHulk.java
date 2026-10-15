package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "139")
public class TangleHulk extends Card {

    public TangleHulk() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new RegenerateEffect()), "{2}{G}: Regenerate Tangle Hulk."));
    }
}
