package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "81")
public class OnyxGoblet extends Card {

    public OnyxGoblet() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{T}: Target player loses 1 life."));
    }
}
