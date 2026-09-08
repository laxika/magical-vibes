package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "140")
public class SauroformHybrid extends Card {

    public SauroformHybrid() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(new AdaptEffect(4)),
                "{4}{G}{G}: Adapt 4."
        ));
    }
}
