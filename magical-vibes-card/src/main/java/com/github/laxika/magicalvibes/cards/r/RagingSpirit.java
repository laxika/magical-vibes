package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "188")
public class RagingSpirit extends Card {

    public RagingSpirit() {
        // {2}: This creature becomes colorless until end of turn (self-scoped layer-5 color set).
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BecomeColorlessUntilEndOfTurnEffect(false)),
                "{2}: This creature becomes colorless until end of turn."));
    }
}
