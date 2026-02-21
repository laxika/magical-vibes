package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "305")
public class TrollAscetic extends Card {

    public TrollAscetic() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new RegenerateEffect()), false, "{1}{G}: Regenerate Troll Ascetic."));
    }
}
