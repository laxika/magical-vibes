package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "44")
public class CacklingImp extends Card {

    public CacklingImp() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{T}: Target player loses 1 life."));
    }
}
