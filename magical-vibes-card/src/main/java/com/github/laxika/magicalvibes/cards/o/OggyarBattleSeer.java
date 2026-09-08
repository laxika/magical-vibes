package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "209")
public class OggyarBattleSeer extends Card {

    public OggyarBattleSeer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new ScryEffect(1)),
                "{T}: Scry 1."));
    }
}
