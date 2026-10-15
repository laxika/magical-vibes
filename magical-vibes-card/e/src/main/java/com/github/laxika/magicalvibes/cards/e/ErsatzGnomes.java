package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "301")
public class ErsatzGnomes extends Card {

    public ErsatzGnomes() {
        // {T}: Target spell becomes colorless — an indefinite layer-5 setter with an empty color set
        // (null color = colorless). It carries onto the permanent a permanent spell becomes (CR 400.7a).
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new SetTargetColorEffect(null, true)),
                "{T}: Target spell becomes colorless."));

        // {T}: Target permanent becomes colorless until end of turn.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new BecomeColorlessUntilEndOfTurnEffect(true)),
                "{T}: Target permanent becomes colorless until end of turn."));
    }
}
