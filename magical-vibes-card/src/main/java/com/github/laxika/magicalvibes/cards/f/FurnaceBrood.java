package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "84")
public class FurnaceBrood extends Card {

    public FurnaceBrood() {
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new PreventTargetCreatureRegenerationThisTurnEffect()),
                "{R}: Target creature can't be regenerated this turn."));
    }
}
