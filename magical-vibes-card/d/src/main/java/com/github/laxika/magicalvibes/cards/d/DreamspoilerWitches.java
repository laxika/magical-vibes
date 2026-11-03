package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "108")
@CardRegistration(set = "MMA", collectorNumber = "81")
public class DreamspoilerWitches extends Card {

    public DreamspoilerWitches() {
        // Flying (auto-loaded from Scryfall).
        // Whenever you cast a spell during an opponent's turn, you may have target creature get
        // -1/-1 until end of turn.
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().duringOpponentsTurn(),
                new MayEffect(
                        new BoostTargetCreatureEffect(-1, -1),
                        "Have target creature get -1/-1 until end of turn?"));
    }
}
