package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "203")
public class HurrJackal extends Card {

    public HurrJackal() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PreventTargetCreatureRegenerationThisTurnEffect()),
                "{T}: Target creature can't be regenerated this turn."));
    }
}
