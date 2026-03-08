package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "163")
public class TrespassingSouleater extends Card {

    public TrespassingSouleater() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U/P}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{U/P}: Trespassing Souleater can't be blocked this turn."
        ));
    }
}
