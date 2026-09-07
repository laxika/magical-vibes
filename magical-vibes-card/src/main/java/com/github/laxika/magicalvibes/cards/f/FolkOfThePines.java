package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "235")
@CardRegistration(set = "DKM", collectorNumber = "25")
public class FolkOfThePines extends Card {

    public FolkOfThePines() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new BoostSelfEffect(1, 0)), "{1}{G}: This creature gets +1/+0 until end of turn."));
    }
}
