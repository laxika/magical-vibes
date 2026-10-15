package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "58")
public class CalderaKavu extends Card {

    public CalderaKavu() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: This creature gets +1/+1 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new SetChosenColorUntilEndOfTurnEffect(false, false)),
                "{G}: This creature becomes the color of your choice until end of turn."));
    }
}
