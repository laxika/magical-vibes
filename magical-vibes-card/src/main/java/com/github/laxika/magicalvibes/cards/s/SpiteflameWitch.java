package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "197")
public class SpiteflameWitch extends Card {

    public SpiteflameWitch() {
        // "{B}{R}: Each player loses 1 life."
        addActivatedAbility(new ActivatedAbility(false, "{B}{R}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER)),
                "{B}{R}: Each player loses 1 life."));
    }
}
