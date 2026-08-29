package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "73")
@CardRegistration(set = "JOU", collectorNumber = "52")
public class SigiledStarfish extends Card {

    public SigiledStarfish() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new ScryEffect(1)),
                "{T}: Scry 1."));
    }
}
