package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "113")
public class DarksteelPendant extends Card {

    public DarksteelPendant() {
        addActivatedAbility(new ActivatedAbility(true, "{1}", List.of(new ScryEffect(1)),
                "{1}, {T}: Scry 1."));
    }
}
