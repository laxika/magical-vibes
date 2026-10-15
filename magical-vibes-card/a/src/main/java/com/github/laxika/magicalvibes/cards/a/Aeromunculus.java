package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "152")
public class Aeromunculus extends Card {

    public Aeromunculus() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{U}",
                List.of(new AdaptEffect(1)),
                "{2}{G}{U}: Adapt 1."
        ));
    }
}
