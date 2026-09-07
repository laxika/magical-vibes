package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "29")
public class ScreechingGriffin extends Card {

    public ScreechingGriffin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new CantBlockSourceEffect(null)),
                "{R}: Target creature can't block this creature this turn."
        ));
    }
}
