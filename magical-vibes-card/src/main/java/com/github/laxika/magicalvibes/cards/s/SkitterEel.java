package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "53")
public class SkitterEel extends Card {

    public SkitterEel() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new AdaptEffect(2)),
                "{2}{U}: Adapt 2."
        ));
    }
}
